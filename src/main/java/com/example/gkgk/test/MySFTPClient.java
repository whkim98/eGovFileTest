package com.example.gkgk.test;

import com.jcraft.jsch.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyAgreement;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Scanner;
import java.util.Vector;

public class MySFTPClient {

    public void start(){
        Security.addProvider(new BouncyCastleProvider());
        try{
            KeyPairGenerator.getInstance("DH");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        try{
            KeyAgreement.getInstance("DH");
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        Session session = null;
        Channel channel = null;
        JSch jsch = new JSch();

        Scanner scanner = new Scanner(System.in);
        System.out.println("계정 입력: ");
        String username = scanner.nextLine();
        System.out.println("호스트 주소 입력: ");
        String host = scanner.nextLine();
        System.out.println("비밀번호 입력: ");
        String password = scanner.nextLine();

        try{
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            System.out.println("Connected to user@" + host);
        }catch (JSchException e){
            e.printStackTrace();
            System.out.println("접속 실패");
            System.exit(0);
        }
        ChannelSftp channelSftp = (ChannelSftp) channel;
        while(true){
            System.out.println("sftp>");
            String str = "";
            str = scanner.nextLine();

            String[] params = str.split(" ");
            String command = params[0];

            if(command.equals("cd")){
                String p1 = params[1];
                try{
                    channelSftp.cd(p1);
                } catch (SftpException e) {
                    System.out.println("에러");
                }
            }
            else if(command.equals("lcd")){
                String p1 = params[1];
                try{
                    channelSftp.lcd(p1);
                }catch (SftpException e){
                    System.out.println("에러");
                }
            }
            else if(command.equals("pwd")){
                try{
                    System.out.println(channelSftp.pwd());
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("lpwd")) {
                System.out.println(channelSftp.lpwd());
            } else if (command.equals("get")) {
                try{
                    if(params.length == 2){
                        channelSftp.get(params[1]);
                    }else{
                        channelSftp.get(params[1], params[2]);
                    }
                }catch (SftpException e){
                    e.printStackTrace();
                }
            } else if (command.equals("put")) {
                String p1 = str.split(" ")[1];
                try{
                    channelSftp.put(p1);
                }catch (SftpException e){
                    System.out.println("Ex)put window.txt");
                }
            }
            else if(command.equals("ls") || command.equals("dir")){
                String path = "";
                try{
                    Vector vector = channelSftp.ls(path);
                    if(vector != null){
                        for(int i=0; i<vector.size(); i++){
                            Object obj = vector.elementAt(i);
                            if(obj instanceof ChannelSftp.LsEntry){
                                System.out.println(((ChannelSftp.LsEntry)obj).getLongname());
                            }
                        }
                    }
                }catch (SftpException e){
                    System.out.println(e.toString());
                }
            } else if (command.equals("rm")) {
                try{
                    String p1 = str.split(" ")[1];
                    channelSftp.rm(p1);
                }catch (SftpException e){
                    e.printStackTrace();
                }
            } else if (command.equals("mkdir")) {
                String p1 = str.split(" ")[1];
                try{
                    channelSftp.mkdir(p1);
                }catch (SftpException e){
                    e.printStackTrace();
                }
            } else if (command.equals("rmdir")) {
                String p1 = str.split(" ")[1];
                try{
                    channelSftp.rmdir(p1);
                }catch (SftpException e){
                    e.printStackTrace();
                }
            } else if (command.equals("chmod")) {
                String p1 = str.split(" ")[1];
                String p2 = str.split(" ")[2];
                try{
                    channelSftp.chmod(Integer.parseInt(p1), p2);
                }catch(NumberFormatException e){
                    e.printStackTrace();
                }catch (SftpException e){
                    e.printStackTrace();
                }
            }
            else if(command.equals("chown")){
                String p1 = str.split(" ")[1];
                String p2 = str.split(" ")[2];
                try{
                    channelSftp.chown(Integer.parseInt(p1), p2);
                }catch(NumberFormatException e){
                    e.printStackTrace();
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }
            else if(command.equals("ln") || (command.equals("symlink"))){
                String p1 = str.split(" ")[1];
                String p2 = str.split(" ")[2];
                try{
                    channelSftp.symlink(p1, p2);
                }catch (SftpException e){
                    e.printStackTrace();
                }
            } else if (command.equals("quit")) {
                channelSftp.quit();
                break;
            }
            else{
                System.out.println("Invalid command");
            }
        }
        channelSftp.disconnect();
        scanner.close();
        System.exit(0);
    }

}
