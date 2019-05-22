package net.qiujuer.lesson.sample.client;

import net.qiujuer.lesson.sample.client.bean.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务器性能数据分析
 * 1，CPU：取决于数据的频繁性、数据的转发复杂性
 * 2，内存：取决于客户端的数量、客户端发送的数据大小
 * 3，线程：取决于连接的客户端的数量
 *
 * 服务器优化方案分析：
 * 1，减少线程数量
 * 2，增加线程执行繁忙状态
 * 3，客户端buffer复用机制
 * @author Gryant
 */
public class ClientTest {

    private static boolean done;

    public static void main(String[] args) throws IOException {

        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        // 当前连接数
        int size = 0;
        List<TCPClient> tcpClients = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {

            try {

                TCPClient tcpClient = TCPClient.startWith(info);
                if (tcpClient == null) {
                    System.out.println("连接异常:" + i);
                    continue;
                }

                tcpClients.add(tcpClient);
                System.out.println("连接成功" + (++size));

            } catch (IOException e) {
                System.out.println("连接异常");
            }

            try {
                // 等待一定时间
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.in.read();

        Runnable runnable = () -> {
            while (!done) {
                for (TCPClient tcpClient : tcpClients) {
                    tcpClient.send("Hello~~");
                }

                try {
                    // 等待一定时间
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

        System.in.read();
        done = true;
        try {
            // 等待线程执行结束
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 退出所有客户端
        tcpClients.forEach(e -> e.exit());
    }
}
