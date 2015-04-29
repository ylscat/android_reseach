package arbell.research;

import com.baidu.mapapi.SDKInitializer;

/**
 * @author YinLanshan
 *         creation time 2015/4/20.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
