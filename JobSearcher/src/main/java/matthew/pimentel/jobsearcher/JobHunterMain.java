/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package matthew.pimentel.jobsearcher;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Matt
 */
public class JobHunterMain {

    public static void main(String[] args) throws IOException {
        String userHomeFolder = System.getProperty("user.home") + "/Desktop";
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter the job title you would like to search for!");
        String jobTitle = input.next();
        input.nextLine();
        System.out.println("Please enter your location!");
        String jobLocation = input.next();
        input.nextLine();
        File file = new File(userHomeFolder, "JobSearch.csv");
        
        if(file.exists()){
            file.delete();
        }
        
        FileWriter output = new FileWriter(file);
        CSVWriter write = new CSVWriter(output);
        String[] header = {"Title", "Description", "Company", "Location", "Salary", "URL"};
        write.writeNext(header);
        try {
            output = new FileWriter(file);
        } catch (IOException ex) {
            Logger.getLogger(JobHunterMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        String url = "https://ca.indeed.com/jobs?q=" + jobTitle + "&l=" + jobLocation + "&as_any&as_not&as_ttl&as_cmp&jt=all&st&salary&radius=25&l&fromage=any&limit=50&sort&psf=advsrch&from=advancedsearch";

        WebClient client = new WebClient();
        client.getOptions().setJavaScriptEnabled(false);
        client.getOptions().setCssEnabled(false);
        client.getOptions().setUseInsecureSSL(true);

        try {
            HtmlPage page = client.getPage(url);
            List<HtmlElement> jobs = page.getByXPath("//div[@id='mosaic-zone-jobcards']/div/a/div[@class='slider_container']");
            HtmlPage page1 = client.getPage(url);
            List<HtmlElement> jobs1 = page1.getByXPath("//a[starts-with(@class, 'tapItem')]");
            if (jobs.isEmpty() || jobs1.isEmpty()) {
                System.out.println("No Jobs Found!");
            } else {
                for (int i = 0; i < jobs.size(); i++) {
                    HtmlAnchor jobAnchor = ((HtmlAnchor) jobs1.get(i).getFirstByXPath("div[@class='slider_container']/parent::a"));
                    HtmlElement spanTitle = ((HtmlElement) jobs.get(i).getFirstByXPath("div[@class='slider_list']/div/div/table/tbody/tr/td/div/h2/span"));
                    HtmlElement company = ((HtmlElement) jobs.get(i).getFirstByXPath("div[@class='slider_list']/div/div/table/tbody/tr/td/div/pre/span"));
                    HtmlElement location = ((HtmlElement) jobs.get(i).getFirstByXPath("div[@class='slider_list']/div/div/table/tbody/tr/td/div/pre/div"));
                    HtmlElement salary = ((HtmlElement) jobs.get(i).getFirstByXPath("div[@class='slider_list']/div/div/table/tbody/tr/td/div/div/div/span"));
                    HtmlElement desc = ((HtmlElement) jobs.get(i).getFirstByXPath("div[@class='slider_list']/div/div/table/tbody/tr/td//div/div/ul"));
                    Job job = new Job();
                    job.setTitle(spanTitle.asText());
                    job.setCompany(company.asText());
                    job.setLocation(location.asText());
                    if (salary != null) {
                        job.setSalary(salary.asText());
                    }

                    job.setDescription(desc.asText());
                    job.setUrl("indeed.ca" + jobAnchor.getHrefAttribute());

                    String[] info = {job.getTitle(), job.getDescription(), job.getCompany(), job.getLocation(), job.getSalary(), job.getUrl()};
                    write.writeNext(info);

                    ObjectMapper mapper = new ObjectMapper();
                    String test = mapper.writeValueAsString(job);

                }

            }
        } catch (IOException ex) {
            Logger.getLogger(JobHunterMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FailingHttpStatusCodeException ex) {
            Logger.getLogger(JobHunterMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
