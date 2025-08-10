package com.spring.springbootapplication.controller;

import org.springframework.context.annotation.Profile; 
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.sql.DataSource;
import java.util.*;

@Profile("dev") // ★ devプロファイルのときだけ有効
@RestController
@RequestMapping("/debug")
public class DebugDbInfoController {
    private final DataSource ds;
    public DebugDbInfoController(DataSource ds) { this.ds = ds; }

    @GetMapping("/dbinfo")
    public Map<String, Object> dbinfo() throws Exception {
        var info = new LinkedHashMap<String, Object>();
        try (var conn = ds.getConnection();
            var st = conn.createStatement()) {
            try (var rs = st.executeQuery("select current_database() as db, inet_server_addr() as host")) {
                if (rs.next()) {
                    info.put("current_database", rs.getString("db"));
                    info.put("server_addr", rs.getString("host"));
                }
            }
            try (var rs2 = st.executeQuery("select count(*) from users")) {
                rs2.next();
                info.put("users_count", rs2.getLong(1));
            }
        }
        return info;
    }
}
