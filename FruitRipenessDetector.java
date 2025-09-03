import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

public class FruitRipenessDetector {

    // Load OpenCV DLL
    static {
        System.load("C:\\Users\\CIBI ARULNATH\\cibicv\\cibicv12\\cibicv12\\lib\\opencv_java4110.dll");
    }

    // Detect ripeness based on HSV values
    private static String detectRipeness(Mat frame) {
        // Convert to HSV
        Mat hsv = new Mat();
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

        // Apply blur to reduce noise
        Imgproc.GaussianBlur(hsv, hsv, new Size(5, 5), 0);

        // Color ranges (tune if needed)
        Scalar lowerGreen = new Scalar(35, 50, 50);
        Scalar upperGreen = new Scalar(85, 255, 255);

        Scalar lowerYellow = new Scalar(20, 100, 100);
        Scalar upperYellow = new Scalar(30, 255, 255);

        Scalar lowerRed1 = new Scalar(0, 100, 100);
        Scalar upperRed1 = new Scalar(10, 255, 255);
        Scalar lowerRed2 = new Scalar(160, 100, 100);
        Scalar upperRed2 = new Scalar(179, 255, 255);

        // Masks
        Mat greenMask = new Mat();
        Mat yellowMask = new Mat();
        Mat redMask1 = new Mat();
        Mat redMask2 = new Mat();

        Core.inRange(hsv, lowerGreen, upperGreen, greenMask);
        Core.inRange(hsv, lowerYellow, upperYellow, yellowMask);
        Core.inRange(hsv, lowerRed1, upperRed1, redMask1);
        Core.inRange(hsv, lowerRed2, upperRed2, redMask2);

        Mat redMask = new Mat();
        Core.addWeighted(redMask1, 1.0, redMask2, 1.0, 0.0, redMask);

        // Count pixels
        double greenPixels = Core.countNonZero(greenMask);
        double yellowPixels = Core.countNonZero(yellowMask);
        double redPixels = Core.countNonZero(redMask);

        // Debugging: show masks
        HighGui.imshow("Green Mask", greenMask);
        HighGui.imshow("Yellow Mask", yellowMask);
        HighGui.imshow("Red Mask", redMask);

        // Decide ripeness
        if (redPixels > yellowPixels && redPixels > greenPixels) {
            return "RIPE (Red)";
        } else if (yellowPixels > greenPixels) {
            return "MID-RIPE (Yellow)";
        } else {
            return "UNRIPE (Green)";
        }
    }

    public static void main(String[] args) {
        VideoCapture capture = new VideoCapture(0); // Open webcam

        if (!capture.isOpened()) {
            System.out.println("Error: Cannot open camera!");
            return;
        }

        Mat frame = new Mat();

        while (true) {
            if (!capture.read(frame)) {
                System.out.println("No frame captured!");
                break;
            }

            // Enhance image for better detection (works for showing phone images too)
            Mat enhanced = new Mat();
            frame.convertTo(enhanced, -1, 1.2, 20); // Increase contrast & brightness

            // Detect ripeness
            String ripeness = detectRipeness(enhanced);

            // Draw text
            Imgproc.putText(enhanced, ripeness, new Point(50, 50), Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.2, new Scalar(0, 255, 0), 3);

            // Show output
            HighGui.imshow("Fruit Ripeness Detector", enhanced);

            if (HighGui.waitKey(30) == 27) { // ESC key to exit
                break;
            }
        }

        capture.release();
        HighGui.destroyAllWindows();
    }
}
