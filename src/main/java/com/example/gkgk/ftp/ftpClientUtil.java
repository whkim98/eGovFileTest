package com.example.gkgk.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.jsoup.helper.StringUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ftpClientUtil {

    private final FTPClient ftpClient;

    public ftpClientUtil(String server, int port, String user, String password) throws IOException {
        ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        if (!ftpClient.login(user, password)) {
            throw new IOException("FTP 로그인 에러");
        }
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    }

    //파일을 ftp서버에 업로드
//    public boolean uploadFile(String localFilePath, String remoteFilePath) throws IOException {
//        try (FileInputStream input = new FileInputStream(localFilePath)) {
//
//            // 공백을 URL 인코딩으로 변환
//            String encodedRemoteFilePath = URLEncoder.encode(remoteFilePath, StandardCharsets.UTF_8).replace(" ", "%20");
//            boolean success = ftpClient.storeFile(encodedRemoteFilePath, input);
//
//            if (!success) {
//                System.err.println("FTP서버 업로드 실패: " + ftpClient.getReplyString());
//            }
//            return success;
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//            throw e;
//        }
//    }
    public boolean uploadFile(InputStream inputStream, String remoteFilePath) throws IOException {
        // 공백을 %20으로 대체
        remoteFilePath = URLEncoder.encode(remoteFilePath, StandardCharsets.UTF_8);

        System.out.println("ftp클라 remote" + remoteFilePath);

        boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
        if (!success) {
            System.err.println("FTP 업로드 실패: " + ftpClient.getReplyString());
        }
        return success;
    }





    //ftp서버에서 파일 다운로드
//    public InputStream downloadFile(String remoteFilePath) throws IOException {
//        InputStream inputStream = ftpClient.retrieveFileStream(remoteFilePath);
//        if (inputStream == null) {
//            System.err.println("FTP 다운로드 실패: " + ftpClient.getReplyString());
//            throw new IOException("파일 다운로드에 실패했습니다.");
//        }
//        return inputStream;
//    }

    public boolean downloadFile(String localFilePath, String remoteFilePath) throws IOException {
        try(FileOutputStream output = new FileOutputStream(localFilePath)){
            return ftpClient.retrieveFile(remoteFilePath, output);
        }
    }

    public boolean deleteFile(String remoteFilePath) throws IOException {
        boolean deleted = ftpClient.deleteFile(remoteFilePath);
        System.out.println(remoteFilePath);
        if (!deleted) {
            System.out.println("FTP파일 삭제 실패: " + ftpClient.getReplyString());
        }
        return deleted;
    }


    //ftp서버 연결 종료
    public void disconnect() throws IOException {
        if(ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

}
