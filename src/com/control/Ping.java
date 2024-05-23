package com.control;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.view.MainForm;

import javax.swing.*;

public class Ping{
    private String ipAddress;
    private JTextArea chatBox;
    private int packetCount = 4;
    private int timeout = 1000;

    public Ping(JTextArea chatBox, String ipAddress) {
        this.chatBox = chatBox;
        this.ipAddress = ipAddress;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setPacketCount(int packetCount) {
        this.packetCount = packetCount;
    }

    public void ping() {
        List<Long> responseTimes = new ArrayList<>();

        try {
            InetAddress inet = InetAddress.getByName(ipAddress);
            this.chatBox.append("Pinging " + ipAddress + " with " + packetCount + " packets:\n");
//            System.out.println("Pinging " + ipAddress + " with " + packetCount + " packets:");

            for (int i = 0; i < packetCount; i++) {
                long startTime = System.currentTimeMillis();
                boolean reachable = inet.isReachable(timeout);
                long endTime = System.currentTimeMillis();

                long responseTime = endTime - startTime;
                responseTimes.add(responseTime);

                if (reachable) {
//                    System.out.println("Reply from " + ipAddress + ": time=" + responseTime + "ms");
                    this.chatBox.append("Reply from " + ipAddress + ": time=" + responseTime + "ms\n");
                } else {
//                    System.out.println("Request timed out.");
                    this.chatBox.append("Request timed out.\n");
                }

                // 等待一秒后再发送下一个包
                Thread.sleep(1000);
            }

            // 统计平均响应时间
            long sum = 0;
            int received = 0;
            for (long time : responseTimes) {
                if (time < timeout) { // 只统计成功的响应时间
                    sum += time;
                    received++;
                }
            }

            double averageTime = received > 0 ? (double) sum / received : 0;
            this.chatBox.append("Ping statistics for " + ipAddress + ":\n");
            this.chatBox.append("    Packets: Sent = " + packetCount + ", Received = " + received + ", Lost = " + (packetCount - received) + " (" + (100 * (packetCount - received) / packetCount) + "% loss)\n");
            this.chatBox.append("Approximate round trip times in milli-seconds:\n");
            this.chatBox.append("    Minimum = " + (received > 0 ? responseTimes.stream().min(Long::compareTo).get() : 0) + "ms, Maximum = " + (received > 0 ? responseTimes.stream().max(Long::compareTo).get() : 0) + "ms, Average = " + averageTime + "ms\n");
//            System.out.println("\nPing statistics for " + ipAddress + ":");
//            System.out.println("    Packets: Sent = " + packetCount + ", Received = " + received + ", Lost = " + (packetCount - received) + " (" + (100 * (packetCount - received) / packetCount) + "% loss)");
//            System.out.println("Approximate round trip times in milli-seconds:");
//            System.out.println("    Minimum = " + (received > 0 ? responseTimes.stream().min(Long::compareTo).get() : 0) + "ms, Maximum = " + (received > 0 ? responseTimes.stream().max(Long::compareTo).get() : 0) + "ms, Average = " + averageTime + "ms");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

