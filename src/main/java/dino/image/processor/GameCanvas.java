package dino.image.processor;

import dino.image.processor.object.ObstacleLocation;
import dino.image.processor.object.ObjectWidth;
import dino.util.Constants;
import dino.util.ImageUtility;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class GameCanvas {
    private final BufferedImage image;
    private ObstacleLocation obstacleLocation = ObstacleLocation.NO_OBJECT_DETECTED;
    private int groundObjectWidth = 0;
    private int objectXAxisPoint;

    public GameCanvas(BufferedImage image) {
        this.image = removeDinoFloorAndSkyFromImage(image);
        this.findObject();
    }

    private BufferedImage removeDinoFloorAndSkyFromImage(BufferedImage image) {
        try {
            return image.getSubimage(Constants.DINO_X_AXIS, Constants.DINO_Y_AXIS, image.getWidth() - Constants.DINO_X_AXIS, 35);
        } catch (Exception ignored) {
            return image;
        }
    }

    public DataBufferByte imageDataBuffer() {
        return (DataBufferByte) image.getRaster().getDataBuffer();
    }


    private boolean isAnyPixelFoundAtBottomFrom(int currentXAxisLocation) {
        int traverseYAxis = image.getHeight() - 1;
        if (new ImageUtility(image).isGrayPixel(currentXAxisLocation, traverseYAxis)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private void findObject() {
        int firstPixelFoundAt = Constants.PIXEL_NOT_FOUND;
        for (int X_AXIS = 0; X_AXIS < image.getWidth(); X_AXIS++) {
            if (new ImageUtility(image).isAnyPixelFoundAtTop(X_AXIS)) {
                firstPixelFoundAt = setFirstPixelValue(firstPixelFoundAt, X_AXIS);
                this.obstacleLocation = ObstacleLocation.IN_THE_SKY;
            }
            if (isAnyPixelFoundAtBottomFrom(X_AXIS)) {
                firstPixelFoundAt = setFirstPixelValue(firstPixelFoundAt, X_AXIS);
                this.obstacleLocation = ObstacleLocation.CLOSER_TO_THE_GROUND;
                this.groundObjectWidth = new ObjectWidth(this.objectXAxisPoint, this.image).determineWidthOfTheGroundObject();
            }
            if (firstPixelFoundAt != Constants.PIXEL_NOT_FOUND && (X_AXIS - firstPixelFoundAt) > Constants.PIXELS_BUFFER) {
                break;
            }
        }
    }

    private int setFirstPixelValue(int firstPixelFoundAt, int X_AXIS) {
        if (firstPixelFoundAt == Constants.PIXEL_NOT_FOUND) {
            this.objectXAxisPoint = X_AXIS;
            return X_AXIS;
        } else {
            return firstPixelFoundAt;
        }
    }

    public ObstacleLocation objectLocation() {
        return this.obstacleLocation;
    }

    public boolean isLongGroundObject() {
        return groundObjectWidth >= Constants.CLUSTERED_CACTUS_SIZE;
    }

    public int getGroundObjectWidth() {
        return groundObjectWidth;
    }

    public int distanceFromObject() {
        int firstPixelWasFoundAt = this.objectXAxisPoint;
        if (isLongGroundObject()) {
            return firstPixelWasFoundAt + getGroundObjectWidth();
        } else {
            return firstPixelWasFoundAt;
        }
    }

    public ObstacleLocation performGroundAction() {
        return distanceFromObject() < Constants.JUMP_SAFE_DISTANCE ? ObstacleLocation.CLOSER_TO_THE_GROUND : ObstacleLocation.NO_OBJECT_DETECTED;
    }

    public ObstacleLocation performFlyingAction() {
        return distanceFromObject() <= Constants.JUMP_SAFE_DISTANCE ? ObstacleLocation.IN_THE_SKY : ObstacleLocation.NO_OBJECT_DETECTED;
    }

    public ObstacleLocation getNextObstacleLocation() {
        if (objectLocation() == ObstacleLocation.CLOSER_TO_THE_GROUND) {
            return performGroundAction();
        } else if (objectLocation() == ObstacleLocation.IN_THE_SKY) {
            return performFlyingAction();
        } else {
            return ObstacleLocation.NO_OBJECT_DETECTED;
        }
    }
}