package github.sangwook.ecommerce.integration.order;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * Hibernate가 실제로 준비(prepare)하는 SQL을 가로채서 기록한다.
 * hibernate.session_factory.statement_inspector 속성으로 등록한다.
 */
public class SqlCaptor implements StatementInspector {

    private static final List<String> SQLS = new CopyOnWriteArrayList<>();

    @Override
    public String inspect(String sql) {
        SQLS.add(sql);
        return sql;
    }

    public static void clear() {
        SQLS.clear();
    }

    public static List<String> sqls() {
        return List.copyOf(SQLS);
    }

    public static long count(String prefix) {
        return SQLS.stream()
            .map(s -> s.trim().toLowerCase())
            .filter(s -> s.startsWith(prefix))
            .count();
    }

    public static void print(String title) {
        System.out.println("\n===== " + title + " =====");
        SQLS.forEach(s -> System.out.println("  " + s));
        System.out.printf("  -> insert order_item: %d, update order_item: %d, delete order_item: %d%n",
            count("insert into order_item"),
            count("update order_item"),
            count("delete from order_item"));
        System.out.println("==========================\n");
    }
}