package io.iconator.rates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EntityScan(basePackages = {"io.iconator.commons.model.db"})
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EnableAutoConfiguration
@ComponentScan(basePackages = {"io.iconator.commons.sql.dao"})
public class RatesApplication {
    private static final Logger LOG = LoggerFactory.getLogger(RatesApplication.class);

    public static void main(String[] args) {
        try {
            run(RatesApplication.class, args);
        } catch (Throwable t) {
            LOG.error("cannot execute rates", t);
        }
    }

//    public void run(String... args) throws Exception {
//        if (options.getExportFile() != null) {
//            File file = new File(options.getExportFile());
//            FileWriter fileWriter = new FileWriter(file);
//            PrintWriter printWriter = new PrintWriter(fileWriter);
//            for (ExchangeRate p : repository.findAllByOrderByCreationDate()) {
//                printWriter.print(p.getCreationDate().getTime());
//                printWriter.print(",");
//                printWriter.print(p.getBlockNrBtc());
//                printWriter.print(",");
//                printWriter.print(p.getBlockNrEth());
//                printWriter.print(",");
//                printWriter.print(p.getRateBtc());
//                printWriter.print(",");
//                printWriter.print(p.getRateBtcBitfinex());
//                printWriter.print(",");
//                printWriter.print(p.getRateIotaBitfinex());
//                printWriter.print(",");
//                printWriter.print(p.getRateEth());
//                printWriter.print(",");
//                printWriter.print(p.getRateEthBitfinex());
//            }
//            fileWriter.close();
//        }
//    }
}
