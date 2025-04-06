package newpackage;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Point;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;S

/**
 * 
 * @author Enes KOC
 */
public class Detector {
    private Mat originalFrame = null;    
    private Mat cloneFrame = null;
    
    public Detector(){
    }
    
    public Detector(Mat frame) {
        this();
        this.originalFrame = frame;
        this.cloneFrame = frame.clone();
    }
    
    public Mat getFrame(){
        return this.originalFrame;
    }
    
    public void setFrame(Mat frame){
        this.originalFrame = frame;
        this.cloneFrame = frame.clone();
    }

    public List<Rect> detect(boolean isDrawed){
        //Imgproc.resize(frame, frame, new Size(0, 0), 0.5, 0.5);

        // Resimdeki gürültüleri temizle
        // Resmi yumuşatma ve gürültüyü temizleme için ayrıca: bilateralFilter
        Photo.fastNlMeansDenoising(this.cloneFrame, this.cloneFrame, 10);

        // Resmi gri renk tonuna dönüştür.
        Imgproc.cvtColor(this.cloneFrame, this.cloneFrame, Imgproc.COLOR_BGR2GRAY);
        
        // Resimleri kolaylıkla bölümlere ayırmak için Eşikleme (Thresholding) işlemini gerçekleştiriyoruz. 
        // Ayrıca gri renk tonlu bir görüntüden ikili görüntüler oluşturuyoruz.
        Imgproc.adaptiveThreshold(this.cloneFrame, this.cloneFrame, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 2);

        // Suzuki85 algoritmasını kullanarak ikili görüntüden konturları alır. 
        // Konturlar, şekil analizi ve nesne algılama ve tanıma için kullanışlı bir araçtır.
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(this.cloneFrame, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Kontur sayısını azaltıyoruz.
        contours = filterContours(contours);

        if(isDrawed)
            Imgproc.drawContours(this.originalFrame, contours, -1, new Scalar(0, 255, 0));

        return getRect(contours);
    }
    
    private List<MatOfPoint> filterContours(List<MatOfPoint> contours) {
        List<MatOfPoint> filteredContours = new ArrayList<>();
        MatOfPoint2f curve = new MatOfPoint2f();
        
        for(MatOfPoint c : contours){
            c.convertTo(curve, CvType.CV_32FC2);
            
            if (isPlate(c, curve))
                filteredContours.add(c);
        }

        return filteredContours;
    }
    
    private boolean isPlate(MatOfPoint c, MatOfPoint2f curve){
        List<Point> p;
        double minX, maxX, minY, maxY;
        
        p = c.toList();
        
        // Minimum X degeri
        minX = p.stream().min((Point p1, Point p2)->{return (p1.x < p2.x) ? -1 : ((p1.x == p2.x) ? 0 : 1);}).get().x;
        
        // Maksimum X degeri
        maxX = p.stream().max((Point p1, Point p2)->{return (p1.x < p2.x) ? -1 : ((p1.x == p2.x) ? 0 : 1);}).get().x;
        
        // Minimum Y degeri
        minY = p.stream().min((Point p1, Point p2)->{return (p1.y < p2.y) ? -1 : ((p1.y == p2.y) ? 0 : 1);}).get().y;
        
        // Maksimum Y degeri
        maxY = p.stream().max((Point p1, Point p2)->{return (p1.y < p2.y) ? -1 : ((p1.y == p2.y) ? 0 : 1);}).get().y;
        
        // Plakalarin buyuklukleri sabit oldugu icin bu orandan faydalandik. Sadece otomobilleri dikkate aldik. 11x52 cm plaka boyutlari
        double ratio1 = (maxX - minX) / (maxY - minY);
        
        // Dikdortgen bolgenin cevresi
        double perimeter = 2 * (maxX - minX) + 2 * (maxY - minY);
        
        // Dikdortgen bolgenin alani
        double area = Imgproc.contourArea(c);
        
        // Egrinin cevresi
        double length = Imgproc.arcLength(curve, true);
        
        // Ornek verilere gore bu araliklara karar verildi.
        return (ratio1 >= 3.4 && ratio1 <= 5) && (area > 800 && area < 120000) && (length >= perimeter - 40);
    }
    
    private List<Rect> getRect(List<MatOfPoint> contours){
        List<MatOfPoint> selectedContourList = new ArrayList<>();
        MatOfPoint2f curve = new MatOfPoint2f();
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        double epsilon;
        for(MatOfPoint c : contours){
            c.convertTo(curve, CvType.CV_32FC2);
            // 0.0373 degerine, ornek verilere bakarak karar verdik.
            epsilon = Imgproc.arcLength(curve, true) * 0.0666;
            // approxPolyDP() fonksiyonu; bir egriyi veya bir cokgeni daha az kosesi olan başka bir egri/cokgen ile yaklastirarak aralarindaki mesafenin belirtilen kesinlige esit veya daha az olmasini saglar. 
            // Bu fonksiyon Douglas-Peucker algoritmasini kullanir. 
            Imgproc.approxPolyDP(curve, approxCurve, epsilon, true);
            // Ornek verilere gore bu degere karar verildi.
            if(approxCurve.total() >= 4 && approxCurve.total() <= 4)
                selectedContourList.add(c);
        }

        List<Rect> rects = new ArrayList<>();
        for(MatOfPoint c : selectedContourList)
            rects.add(Imgproc.boundingRect(c));
        return rects;
    }
}
