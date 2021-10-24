package org.evrete.showcase.stock;

import org.evrete.Configuration;
import org.evrete.KnowledgeService;
import org.evrete.api.Knowledge;
import org.evrete.showcase.shared.Utils;
import org.evrete.showcase.stock.rule.Constants;
import org.evrete.showcase.stock.rule.EmaRuleset;
import org.evrete.showcase.stock.rule.TradingRuleset;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContext implements ServletContextListener {
    private static KnowledgeService knowledgeService;
    private static Knowledge knowledge;
    static String DEFAULT_STOCK_HISTORY;

    static Knowledge knowledge() {
        if (knowledge == null) {
            throw new IllegalStateException();
        } else {
            return knowledge;
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (knowledgeService == null) {
            try {
                DEFAULT_STOCK_HISTORY =
                        Utils.readResourceAsString(sce.getServletContext(), "/WEB-INF/default_stock_data.json");
                knowledgeService = new KnowledgeService();
                knowledge = knowledgeService.newKnowledge(
                        "JAVA-CLASS", EmaRuleset.class, TradingRuleset.class
                );
            } catch (Exception e) {
                throw new IllegalStateException("Can not read default rule sources", e);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (knowledgeService == null) {
            throw new IllegalStateException();
        } else {
            knowledgeService.shutdown();
        }
    }
}