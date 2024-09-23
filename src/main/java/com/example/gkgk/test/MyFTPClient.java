package com.example.gkgk.test;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import javax.imageio.IIOException;
import java.io.*;
import java.util.Scanner;

public class MyFTPClient {
    public void start(){
        FTPClient ftpClient = new FTPClient();
        Scanner scanner = new Scanner(System.in);
        System.out.println("호스트 주소 입력: ");
        String server = scanner.nextLine();
        try{
            ftpClient.connect(server, 21);
            if(FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
                System.out.println(server + "에 연결되었습니다.");
                System.out.println(ftpClient.getReplyString() + "SUCCESS CONNECTION");
            }
        } catch (Exception e) {
            System.out.println("서버 연결 실패");
            System.exit(0);
        }

        System.out.println("계정 입력: ");
        String user = scanner.nextLine();
        System.out.println("비밀번호 입력: ");
        String password = scanner.nextLine();
        try{
            ftpClient.login(user, password);
            System.out.println(ftpClient.getReplyCode() + "로그인 성공");
        }catch(IOException e){
            System.out.println(ftpClient.getReplyCode() + "로그인 실패");
            System.exit(0);
        }

        while(true){
            System.out.println("ftp>");
            String str = "";
            str = scanner.nextLine();
            String[] params = str.split(" ");
            String command = params[0];

            if(command.equals("cd")){
                String path = params[1];
                try{
                    ftpClient.changeWorkingDirectory(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(command.equals("mkdir")){
                String directory = params[1];
                try{
                    ftpClient.makeDirectory(directory);
                }catch(IOException e){
                    e.printStackTrace();
                }
            } else if (command.equals("rmdir")) {
                String directory = params[1];
                try{
                    ftpClient.removeDirectory(directory);
                }catch(IOException e){
                    e.printStackTrace();
                }
            } else if (command.equals("binary")) {
                try{
                    ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                    System.out.println(ftpClient.getReplyCode() + "Binary Mode");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("ascii")) {
                try{
                    ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
                    System.out.println(ftpClient.getReplyCode() + "Ascii Mode");
                }catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("pwd")) {
                try{
                    System.out.println(ftpClient.printWorkingDirectory());
                }catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("quit")) {
                try{
                    ftpClient.logout();
                    System.out.println(ftpClient.getReplyCode() + "로그아웃 완료");
                    break;
                }catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("put")) {
                String p1 = str.split(" ")[1];
                String p2 = str.split(" ")[2];
                File putFile = new File(p2);
                InputStream inputStream = null;

                try{
                    inputStream = new FileInputStream(putFile);
                    boolean result = ftpClient.storeFile(p1, inputStream);
                    if(result == true){
                        System.out.println("업로드 완료");
                    }else{
                        System.out.println("업로드 실패");
                    }
                } catch(IOException e){
                    e.printStackTrace();
                }finally {
                    if(inputStream != null){
                        try{
                            inputStream.close();
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (command.equals("get")) {
                String p1 = str.split(" ")[1];
                String p2 = str.split(" ")[2];
                File getFile = new File(p2);
                OutputStream outputStream = null;

                try{
                    outputStream = new FileOutputStream(getFile);
                    boolean result = ftpClient.retrieveFile(p1, outputStream);
                    if(result == true){
                        System.out.println("다운로드 완료");
                    }else{
                        System.out.println("다운로드 실패");
                    }
                }catch(FileNotFoundException e1){
                    e1.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(outputStream != null){
                        try{
                            outputStream.close();
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (command.equals("delete")) {
                String file = str.split(" ")[1];
                try{
                    ftpClient.deleteFile(file);
                }catch(IOException e){
                    e.printStackTrace();
                }
            } else if (command.equals("ls")) {
                String[] files = null;
                try{
                    files = ftpClient.listNames();
                    for(int i = 0; i < files.length; i++){
                        System.out.println(files[i]);
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("dir")) {
                FTPFile[] files = null;
                try{
                    files = ftpClient.listFiles();
                    for(int i = 0; i < files.length; i++){
                        System.out.println(files[i]);
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try{
            ftpClient.disconnect();
        }catch (IOException e) {
            e.printStackTrace();
        }finally {
            scanner.close();
        }
        System.exit(0);
    }
}
