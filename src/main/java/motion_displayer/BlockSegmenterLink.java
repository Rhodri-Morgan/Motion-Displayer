package motion_displayer;

import java.util.LinkedList;
import org.opencv.core.Mat;
import org.opencv.core.Rect;


public class BlockSegmenterLink implements VideoProcessorLink {

    private VideoProcessorLink next;

    @Override
    public void handle(VideoFile video) {
        for (Mat frame : video.getUnmodifiedFrames()) {
            int current_x = 0;
            int current_y = 0;
            while (true) {
                if (current_x >= video.getFrameWidth() || current_y >= video.getFrameHeight()) {
                    frame.setBlocks(blocks);
                    break;
                } else {
                    Rect block_roi = new Rect(current_x, current_y, video.getBlockWidth()-1, video.getBlockHeight()-1);
                    Mat block = new Mat(frame.getFrame(), block_roi);
                    Mat temp_block = new Mat();
                    block.copyTo(temp_block);
                    Rect search_area = new Rect(current_x, current_y, video.getBlockWidth()-1, video.getBlockHeight()-1);

                    current_x += video.getBlockWidth();
                    current_y += video.getBlockHeight();
                }
            }
        }
        if (this.next != null) {
            this.next.handle(video);
        }
    }

    @Override
    public void addLink(VideoProcessorLink link) {
        if (this.next != null) {
            this.next.addLink(link);
        } else {
            this.next = link;
        }
    }
}
