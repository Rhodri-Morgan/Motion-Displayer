package motion_displayer.model;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.time.Duration;
import org.opencv.core.Mat;


public class VideoProcessor {

    private int frames_processed = 0;
    private final VideoFile video;
    private long summed_processing_time_ms;

    public VideoProcessor(VideoFile video) {
        this.video = video;
    }

    private Mat extractNextFrame() {
        Mat frame = new Mat();
        if (this.video.getVideoCapture().read(frame)) {
            Mat temp_frame = new Mat();
            frame.copyTo(temp_frame);
            return temp_frame;
        } else {
            return null;
        }
    }

    private void writeFrame(Mat frame) throws FileNotFoundException {
        this.video.getVideoWriter().write(frame);
        if (!Files.exists(video.getOutPath())) {
            throw new FileNotFoundException();
        }
    }

    public void processVideo() throws Exception {
        Mat frame;
        Mat previous_frame = null;
        CalculateMotionVector calculate_motion_vector = new CalculateMotionVector(this.video.getFrameWidth(),
                                                                                  this.video.getFrameHeight(),
                                                                                  this.video.getSearchSize(),
                                                                                  this.video.getBlockSize(),
                                                                                  this.video.getArrowColour());
        while (true) {
            long start = System.nanoTime();
            frame = this.extractNextFrame();
            if (previous_frame == null) {
                this.writeFrame(frame);
                previous_frame = frame;
                this.frames_processed += 1;
            }
            else if (frame != null) {
                Mat processed_frame = calculate_motion_vector.processFrame(frame, previous_frame);
                this.writeFrame(processed_frame);
                this.frames_processed += 1;
            }
            else {
                this.frames_processed = this.video.getFrameCount();
                break;
            }
            long end = System.nanoTime();
            long duration_ms = (end - start) / 1000000;
            this.summed_processing_time_ms += duration_ms;
        }
        this.video.getVideoWriter().release();
        this.video.getVideoCapture().release();
    }

    public double getProgress() {
        return (double) this.frames_processed / this.video.getFrameCount();
    }

    public int getProcessedFrames() {
        return this.frames_processed;
    }

    public Duration getEstimatedProcessingTime() {
        long average_time_taken_ms = this.summed_processing_time_ms / this.frames_processed;
        long estimated_time_ms = average_time_taken_ms * (this.video.getFrameCount() - this.frames_processed);
        return Duration.ofMillis(estimated_time_ms);
    }
}