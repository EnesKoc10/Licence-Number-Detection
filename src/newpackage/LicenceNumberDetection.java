package newpackage;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

/**
 * 
 * @author Enes KOC
 */
public class LicenceNumberDetection extends JFrame implements Runnable, Thread.UncaughtExceptionHandler {
    private VideoCapture videoCapture = null;
    private JLabel jLabel1 = null;
    private Mat frame = null;
    private Detector detector = null;

    private LicenceNumberDetection(){
    }
    
    private void initComponents() {        
        jLabel1 = new JLabel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        getContentPane().add(jLabel1, BorderLayout.CENTER);

        pack();
        setVisible(true);
//        setResizable(false);
        
//        int width = (int)videoCapture.get(3);
//        int height = (int)videoCapture.get(4);
//        setSize(width + 20, height + 40);
//        setTitle(width + ":" + height);
        setSize(frame.width(), frame.height());
        setLocationRelativeTo(null);
        
        detector = new Detector(frame);
    }
    
    private void initCamera(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        videoCapture = new VideoCapture();
//        if(!videoCapture.open(0)){
//            System.err.println("Kamera açılmadı.");
//            System.exit(-1);
//        }
        frame = new Mat();
        frame = Imgcodecs.imread(".\\src\\newpackage\\picture\\test1.jpg");
    }
    
     public static void start() {
        // <editor-fold defaultstate="collapsed" desc="SwingUtilities.invokeLater">
        /**
         * Farklı thread'lar üzerinde oluşturulan bileşenlere sadece aynı thread üzerinden erişim 
         * yaparsak sorun yaşamayız. Bu uygulamada kullandığımız Graphical User Interfaces (GUI) 
         * kontrolleri farklı bir Thread üzerinde başlatılırsa ve başka bir thread'dan bu GUI 
         * kontrollerine erişim yapıldığında sorunlar olabilir. Bu sorunun üstesinden gelmek için 
         * Java'da invokeLater isimli metodu kullanıyoruz. Ayrıca burada kullandığımız kontrollerin 
         * Swing kontrolleri olduğuna dikkat ediniz. Swing ile iş parçacığı oluşturmaya çalıştığımızda, 
         * kullanıcı arayüzündeki tüm güncellemeler olay sevketme thread'ında (The Event Dispatch Thread) 
         * gerçekleşir. Bu nedenle, diğer herhangi bir thread'dan GUI güncelleme kodunu, 
         * Event Dispatch Thread'ından çağrılacak şekilde düzenlememiz gerekir.
         * 
         * Aşağıdaki açıklamalar için link: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html
         * The Event Dispatch Thread (Olay Sevketme Threadı): Swing olay işleme kodu, Event Dispatch Thread'ı 
         * olarak bilinen özel bir thread'da çalışır. Swing yöntemlerini çağıran çoğu kod da bu thread üzerinde 
         * çalışır. Bu, çoğu Swing nesne yöntemi "thread safe" olmadığı için gereklidir: Bunları birden çok thread'dan 
         * çağırmak, thread engelleme veya bellek tutarlılığı hataları riskine neden olur. Bazı Swing bileşen yöntemleri, 
         * API belirtiminde "thread safe" olarak etiketlenmiştir. Bunlar herhangi bir thread'dan güvenle çağrılabilir. 
         * Diğer tüm Swing bileşeni yöntemleri, Event Dispatch Thread'ından çağrılmalıdır. Bu kuralı göz ardı eden 
         * programlar çoğu zaman düzgün çalışabilir. Ancak yeniden üretilmesi zor olan öngörülemeyen hatalara maruz kalırlar.
         * Event Dispatch Thread'ında çalışan kodu bir dizi kısa görev olarak düşünmek yararlıdır. Görevlerin çoğu, 
         * ActionListener.actionPerformed gibi olay işleme yöntemlerinin çağrılarıdır. Diğer görevler, invokeLater veya 
         * invokeAndWait kullanılarak uygulama koduna göre zamanlanabilir. Event Dispatch Thread'ındaki görevler hızlı bir 
         * şekilde bitmelidir. Diğer türlü, işlenmeyen olaylar yedeklenir ve kullanıcı arayüzü yanıt vermemeye başlar.
         */
         // </editor-fold>
        SwingUtilities.invokeLater(new LicenceNumberDetection());
    }
    
    @Override
    public void run() {
        initCamera();
        initComponents();

        // Form işlemleri gerçekleştirilirken aynı anda da kamera görüntüsünün elde edilmesi ve forma yazdırılması için bir Thread oluşturuyoruz.
        Thread t = new Thread() {
            @Override
            public void run() {
                renderFrames();
            }
        };
        t.setName("Thread1");
        t.setDaemon(true);
        t.setUncaughtExceptionHandler(this);
        t.start();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.err.println(t.getName() + ": " + e.getMessage());
    }
    
    private void renderFrames() {
                
        List<Rect> rects = detector.detect(false);
        for(Rect rect : rects)
            Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 0, 255), 2);

        jLabel1.setIcon(new ImageIcon(FrameToImage(frame)));
    }
    
    public BufferedImage FrameToImage(Mat frame) {
        int colorType = BufferedImage.TYPE_CUSTOM;
        
        if (frame.channels() == 1)
            colorType = BufferedImage.TYPE_BYTE_GRAY;
        else if (frame.channels() == 3)
            colorType = BufferedImage.TYPE_3BYTE_BGR;

        // <editor-fold defaultstate="collapsed" desc="BufferedImage">
        /**
         * BufferedImage sınıfı, erişilebilir bir görüntü verisi arabelleğine sahip bir Görüntüyü tanımlar. 
         * BufferedImage, bir ColorModel ve bir Raster görüntü verisinden oluşur. Raster'ın SampleModel'indeki 
         * bantların sayısı ve türleri, ColorModel'in renk ve alfa bileşenlerini temsil etmesi için gereken sayı 
         * ve türlerle eşleşmelidir. Tüm BufferedImage nesnelerinin sol üst köşesi (0, 0) koordinatına sahiptir. 
         * BufferedImage oluşturmak için kullanılan herhangi bir Raster bu nedenle minX=0 ve minY=0 olmalıdır.
         * 
         * Daha fazla bilgi için: https://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
         */
        // </editor-fold>
        BufferedImage bufferedImage = new BufferedImage(frame.width(), frame.height(), colorType);
        
        // <editor-fold defaultstate="collapsed" desc="Raster">
        /**
         * Raster sınıfı, dikdörtgen bir piksel dizisini temsil eden bir sınıftır. Bir Raster, örnek değerleri 
         * depolayan bir DataBuffer'ı ve bir DataBuffer'da belirli bir örnek değerinin nasıl bulunacağını açıklayan 
         * bir SampleModel'i içine alır. Raster, düzlemin belirli bir dikdörtgen alanını kaplayan pikseller için 
         * değerleri tanımlar. Raster'ın sınırlayıcı dikdörtgeni olarak bilinen ve getBounds yöntemiyle kullanılabilen 
         * dikdörtgen; minX, minY, genişlik ve yükseklik değerleriyle tanımlanır. minX ve minY değerleri, Raster'in 
         * sol üst köşesinin koordinatını tanımlar. Sınırlayıcı dikdörtgenin dışındaki piksellere yapılan başvurular, 
         * bir istisnanın atılmasına veya Raster'ın ilişkili DataBuffer'ının istenmeyen öğelerine başvurulara neden olabilir. 
         * Bu tür piksellere erişimden kaçınmak kullanıcının sorumluluğundadır. Bir SampleModel, bir Raster örneklerinin 
         * bir DataBuffer'ın primitive dizi öğelerinde nasıl saklandığını açıklar. Örnekler, PixelInterleavedSampleModel 
         * veya BandedSampleModel'de olduğu gibi veri öğesi başına bir tane saklanabilir veya SinglePixelPackedSampleModel 
         * veya MultiPixelPackedSampleModel'de olduğu gibi birkaç öğeye paketlenebilir. Bir Raster düzlemde herhangi bir 
         * yerde yaşayabilse de, SampleModel (0, 0)'dan başlayan basit bir koordinat sisteminden yararlanır. Bu nedenle 
         * bir Raster, piksel konumlarının Raster'in koordinat sistemi ile SampleModel'inki arasında eşlenmesine izin veren 
         * bir öteleme faktörü içerir. SampleModel koordinat sisteminden Raster'ınkine çeviri, getSampleModelTranslateX ve 
         * getSampleModelTranslateY yöntemleriyle elde edilebilir. Bir Raster, bir DataBuffer'ı başka bir Raster ile açık 
         * bir şekilde inşa ederek veya createChild ve createTranslatedChild yöntemlerini kullanarak paylaşabilir. Bu 
         * yöntemlerle oluşturulan rasterler, getParent yöntemi aracılığıyla oluşturuldukları Raster'a bir başvuru döndürebilir. 
         * createTranslatedChild veya createChild çağrısı yoluyla oluşturulmamış bir Raster için getParent null değerini döndürür.
         * createTranslatedChild yöntemi, geçerli Raster'ın tüm verilerini paylaşan ancak aynı genişlik ve yükseklikte ancak farklı 
         * bir başlangıç ​​noktası olan bir sınırlayıcı dikdörtgeni kaplayan yeni bir Raster döndürür. Örneğin, ana Raster (10, 10) 
         * ile (100, 100) bölgesini işgal ettiyse ve çevrilen Raster (50, 50) ile başlayacak şekilde tanımlanmışsa o zaman ebeveynin 
         * pikseli (20, 20) ve piksel (60, 60), iki Raster tarafından paylaşılan DataBuffer'da aynı konumu işgal eder. İlk durumda, 
         * karşılık gelen SampleModel koordinatını elde etmek için bir piksel koordinatına (-10, -10) eklenmeli ve ikinci durumda 
         * (-50, -50) eklenmelidir. Bir ebeveyn ve alt Raster arasındaki çeviri, çocuğun sampleModelTranslateX ve sampleModelTranslateY 
         * değerlerinin ebeveynin değerlerinden çıkarılmasıyla belirlenebilir. createChild yöntemi, ebeveyninin sınırlayıcı 
         * dikdörtgeninin yalnızca bir alt kümesini veya ebeveyninin bantlarının bir alt kümesini işgal eden yeni bir Raster 
         * oluşturmak için kullanılabilir. Raster oluşturmanın doğru yolu, bu sınıfta tanımlanan statik oluşturma yöntemlerinden 
         * birini kullanmaktır.
         * 
         * Daha fazla bilgi için: https://docs.oracle.com/javase/7/docs/api/java/awt/image/Raster.html
         */
         // </editor-fold>
        WritableRaster writableRaster = bufferedImage.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) writableRaster.getDataBuffer();
        byte[] buffer = dataBuffer.getData();
        frame.get(0, 0, buffer);

        return bufferedImage;
    }
}
