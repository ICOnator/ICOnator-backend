package io.modum.tokenapp.minting;

import io.modum.tokenapp.minting.model.Payin;
import io.modum.tokenapp.minting.model.Token;
import io.modum.tokenapp.minting.service.Minting;
import org.kohsuke.args4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

@ComponentScan(basePackages={
        "io.modum.tokenapp.minting",
        "io.modum.tokenapp.rates"})
@EntityScan(basePackages = {
        "io.modum.tokenapp.backend.model",
        "io.modum.tokenapp.rates.model",
        "io.modum.tokenapp.minting.model"})
@SpringBootApplication
public class MintingApplication implements CommandLineRunner {

    @Autowired
    private Minting minting;

    @Option(name="-p",usage="output file to store the CVS payin data")
    private String payin;

    @Option(name="-t",usage="token distribution, output file the token distribution, input is the CVS payin")
    private String tokenDistribution;

    @Argument
    private List<String> arguments = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MintingApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebEnvironment(false);
        app.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            System.err.println("java MintingApplication [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            System.err.println("  Example: java MintingApplication"+parser.printExample(OptionHandlerFilter.ALL));
            return;
        }

        if(payin != null && tokenDistribution == null) {
            File file = new File(payin);
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            List<Payin> list = minting.payin();
            for(Payin p:list) {
                printWriter.print(p.getCreationDate().getTime());
                printWriter.print(",");
                printWriter.print(p.getTime().getTime());
                printWriter.print(",");
                //
                printWriter.print(p.getBlockNrBtc());
                printWriter.print(",");
                printWriter.print(p.getBlockNrEth());
                printWriter.print(",");
                //
                printWriter.print(p.getWei());
                printWriter.print(",");
                printWriter.print(p.getSatoshi());
                printWriter.print(",");
                //
                printWriter.print(p.getWalletAddress());
            }
            fileWriter.close();
        } else if(payin != null && tokenDistribution != null) {
            BufferedReader buffer=new BufferedReader(new FileReader(new File(payin)));
            String line=null;
            List<Payin> list = new ArrayList<>();
            while((line=buffer.readLine())!=null) {
                StringTokenizer st = new StringTokenizer(line,",");
                Payin p=new Payin();
                p.setCreationDate(new Date(Long.parseLong(st.nextToken())));
                p.setTime(new Date(Long.parseLong(st.nextToken())));
                p.setBlockNrBtc(Long.parseLong(st.nextToken()));
                p.setBlockNrEth(Long.parseLong(st.nextToken()));
                p.setWei(Long.parseLong(st.nextToken()));
                p.setSatoshi(Long.parseLong(st.nextToken()));
                p.setWalletAddress(st.nextToken());
                list.add(p);
            }
            buffer.close();
            List<Token> tokens = minting.calc(list);

            File file = new File(tokenDistribution);
            FileWriter fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            for(Token t:tokens) {
                printWriter.print(t.getCreationDate().getTime());
                printWriter.print(",");
                printWriter.print(t.getAmount());
                printWriter.print(",");
                printWriter.print(t.getWalletAddress());
            }
            fileWriter.close();


        } else if(payin == null && tokenDistribution != null) {
            BufferedReader buffer=new BufferedReader(new FileReader(new File(tokenDistribution)));
            String line=null;
            List<Token> list = new ArrayList<>();
            while((line=buffer.readLine())!=null) {
                StringTokenizer st = new StringTokenizer(line,",");
                Token t=new Token();
                t.setCreationDate(new Date(Long.parseLong(st.nextToken())));
                t.setAmount(Integer.parseInt(st.nextToken()));
                t.setWalletAddress(st.nextToken());
                list.add(t);
            }
            buffer.close();
            minting.mint(list);
        } else {
            System.err.println("parse error");
        }
    }
}
