import entities.User;
import orm.em_builder.EntityManagerBuilder;
import orm.manager.EntityManager;
import orm.strategies.DropCreateStrategy;
import orm.strategies.UpdateStrategy;

import java.util.Date;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        String username = scanner.nextLine().trim();
        String password = scanner.nextLine().trim();
        String adapter = scanner.nextLine().trim();
        String host = scanner.nextLine().trim();
        String port = scanner.nextLine().trim();
        String driver = scanner.nextLine().trim();
        String dbName = scanner.nextLine().trim();

        //Get username, password, adapter, driver, host, port and database name from user input

        EntityManagerBuilder emBuilder = new EntityManagerBuilder(dbName);
        EntityManager em = emBuilder.configureConnectionString()
                .setUser(username)
                .setPass(password)
                .setAdapter(adapter)
                .setDriver(driver)
                .setHost(host)
                .setPort(port)
                .createConnection()
                .configureCreationType().set(UpdateStrategy.class)
                .build();

        User user1 = new User("test1", 19, new Date());
        User user2 = new User("test2", 19, new Date());
        User user3 = new User("test3", 19, new Date());
        User user4 = new User("test4", 19, new Date());
        em.persist(user1);
        em.persist(user2);
        em.persist(user3);
        em.persist(user4);



        //username = entity_scanner.nextLine().trim();
        //password = entity_scanner.nextLine().trim();
        //db = entity_scanner.nextLine().trim();
    }
}
