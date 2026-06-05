package com.kyexam.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kyexam")
@MapperScan("com.kyexam.**.mapper")
public class KyAdminApplication {
    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("  ______                                                               ");
        System.out.println(" /_  __/___  ____  ____ _____ _____ _                                  ");
        System.out.println("  / / / __ \\/ __ \\/ __ `/ __ `/ __ `/                                ");
        System.out.println(" / / / /_/ / /_/ / /_/ / /_/ / /_/ /                                   ");
        System.out.println("/_/  \\____/\\____/\\__, /\\__,_/\\__, /                                    ");
        System.out.println("                /____/      /____/                                     ");
        System.out.println("                       _.._        _.._                                ");
        System.out.println("                     .' .-'`_...._`'-. '.                              ");
        System.out.println("                    /  .'  _......_  '.  \\                             ");
        System.out.println("                   |  /  .'        '.  \\  |                            ");
        System.out.println("                   | |  /            \\  | |   ( Code Less,             ");
        System.out.println("                  _| |  |    _  _    |  | |_    Think More. )          ");
        System.out.println("                 (( \\ \\  \\  (o)(o)  /  / / ))                          ");
        System.out.println("                  '.|  \\  '--'  '--'  /  |.'                           ");
        System.out.println("                    \\   '------------'   /                             ");
        System.out.println("                     '  .  ________  .  '                              ");
        System.out.println("                        /  \\______/  \\                                 ");
        System.out.println("                       /  /        \\  \\                                ");
        System.out.println("                     .'  /_        _\\  '.                              ");
        System.out.println("                    /___;' `-____-' ';___\\                             ");
        System.out.println("=======================================================================");
        System.out.println(" >>> [ky-exam] 考研自助系统后端服务正在启动...                   ");
        System.out.println("=======================================================================");

        SpringApplication.run(KyAdminApplication.class, args);
        System.out.println("\n----------------------------------------");
        System.out.println("系统启动成功");
        System.out.println("----------------------------------------");
    }
}
