package com.example.gkgk.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProgramStart {
    public static void main(String[] args) {
        String command = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        MyFTPClient myFTPClient = new MyFTPClient();
        MySFTPClient mySFTPClient = new MySFTPClient();
        System.out.println("프로토콜 유형: ");
        try{
            command = br.readLine();
            if(command.equals("ftp") || command.equals("FTP")){
                myFTPClient.start();
            } else if (command.equals("sftp") || command.equals("SFTP")) {
                mySFTPClient.start();
            }else{
                System.out.println("다시 입력");
                System.exit(0);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
