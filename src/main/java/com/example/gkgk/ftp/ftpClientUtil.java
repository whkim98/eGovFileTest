package com.example.gkgk.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

public class ftpClientUtil {

    private final FTPClient ftpClient;

    public ftpClientUtil(String server, int port, String user, String password) throws IOException {
        ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        if (!ftpClient.login(user, password)) {
            throw new IOException("FTP login failed.");
        }
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
    }

    //파일을 ftp서버에 업로드
    public boolean uploadFile(String localFilePath, String remoteFilePath) throws IOException {
        try (FileInputStream input = new FileInputStream(localFilePath)) {
            // 공백을 URL 인코딩으로 변환
            String encodedRemoteFilePath = URLEncoder.encode(remoteFilePath, "UTF-8").replace("+", "%20");
            boolean success = ftpClient.storeFile(encodedRemoteFilePath, input);
            if (!success) {
                System.err.println("FTP서버 업로드 실패: " + ftpClient.getReplyString());
            }
            return success;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }

    //ftp서버에서 파일 다운로드
    public boolean downloadFile(String localFilePath, String remoteFilePath) throws IOException {
        try(FileOutputStream output = new FileOutputStream(localFilePath)){
            return ftpClient.retrieveFile(remoteFilePath, output);
        }
    }

    //ftp서버 연결 종료
    public void disconnect() throws IOException {
        if(ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

}
