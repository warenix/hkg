import org.dyndns.warenix.hkg.HKGController;
import org.dyndns.warenix.hkg.HKGThread;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by warenix on 3/11/15.
 */
public class HKGThreadTest {

    @Test
    public void testingCrunchifyAddition() {

        HKGController controller = new HKGController();
        controller.setHKGListener(new HKGController.HKGListener() {
            @Override
            public void onTopicLoaded(String type, int pageNo, ArrayList<HKGThread> threadList) {

            }

            @Override
            public void onThreadLoaded(HKGThread thread) {
                System.out.println("title:" + thread.mTitle);
                HKGThread.HKGPage page = thread.getPage(1);
//                System.out.println("author:" + page.getAuthor());
                for (HKGThread.HKGReply reply : page.getReplyList()) {
                    System.out.println("<br/>user:" + reply.mUser);
                    System.out.println(String.format("<br/><b>%s</b><br/>" ,reply.mQuote));
                    System.out.println(String.format("<br/>%s<br/>", reply.mContent));
                    System.out.println("<hr />");
                }
            }
        });

        final HKGThread thread = new HKGThread();
        thread.mThreadId = "5718365";
        controller.readThreadByPage(thread, 1);
    }


}
