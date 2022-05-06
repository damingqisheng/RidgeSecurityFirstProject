package com.jsjw.ridgeproject;


import connection.ApplicationDB;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Controller
public class SpringFileUploadController {


    @GetMapping("/uploadPage")
    public String uploadPage() {
        return "upload";
    }

    @GetMapping("/down")
    @ResponseBody
    public void down(@RequestParam("id") int id, HttpServletResponse response) {
        try {
            int len = 0;
            ServletOutputStream servletOutputStream = response.getOutputStream();
            //Get the database connection
            ApplicationDB db = new ApplicationDB();
            Connection con = db.getConnection();

            //Create a SQL statement
            Statement stmt = con.createStatement();

            //Make an insert statement for the login in blanks:
            String query = "SELECT * FROM issue WHERE issue_key = ?";
            //Create a Prepared SQL statement allowing you to introduce the parameters of the query
            PreparedStatement ps = con.prepareStatement(query);

            //Add parameters of the query. Start with 1, the 0-parameter is the INSERT statement itself
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();


            while(rs.next()) {
                InputStream input = rs.getBinaryStream("issue_detail");
                String fileType = rs.getString("file_type");
                response.addHeader("Content-Disposition", "attachment;filename="+ "detail"+fileType);
                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    servletOutputStream.write(buffer);
                }

            }

            servletOutputStream.close();

            //Close the connection. Don't forget to do it, otherwise you're keeping the resources of the server allocated.
            con.close();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    @GetMapping("/searchPage")
    public String searchPage() {
        return "searchPage";
    }

    @GetMapping("/search")
    @ResponseBody
    public void search(HttpServletResponse response, @RequestParam("customer_name") String customer_name) throws IOException {
        String issue_keys = "";
        String customer_names = "";
        String product_names = "";
        String product_versions = "";
        String issue_summaries = "";
        String result="";
        try {
            ApplicationDB db = new ApplicationDB();
            Connection con = db.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = null;

            String query = "SELECT * FROM ridge.issue where customer_name ='"+customer_name+"' order by product_name, product_version";
            PreparedStatement ps = con.prepareStatement(query);
            rs = ps.executeQuery();

            while(rs!=null&&rs.next()){
                issue_keys = issue_keys + rs.getString("issue_key")+",,";
                customer_names= customer_names + customer_name+",,";
                product_names=product_names+rs.getString("product_name")+",,";
                product_versions=product_versions+rs.getString("product_version")+",,";
                issue_summaries=issue_summaries+rs.getString("issue_summary")+",,";
            }

        }catch (Exception e) {
            System.out.print(e);
        }

        response.sendRedirect("/viewInfo?issue_keys="+issue_keys+"&customer_names="+customer_names+"&product_names="+product_names+"&product_versions="+product_versions+"&issue_summaries="+issue_summaries);
    }


    @GetMapping("/viewInfo")
    public String viewInfo() {

        return "viewInfo";
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile multiFile,
                                              @RequestParam("customer_name") String customer_name,
                                              @RequestParam("product_name") String product_name,
                                              @RequestParam("product_version") String product_version,
                                              @RequestParam("issue_summary") String issue_summary
                                                ) {
        String fileName = multiFile.getOriginalFilename();

        String postfix = fileName.substring(fileName.lastIndexOf("."));

        System.out.println(customer_name);
        System.out.println(product_name);
        System.out.println(product_version);
        System.out.println(issue_summary);
        System.out.println(postfix);
        try {
            File file = File.createTempFile(fileName, postfix);
            multiFile.transferTo(file);
            insertIssues(file,customer_name,product_name,product_version,issue_summary,postfix);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok("File upload successfully");
    }
    public void insertIssues(File file, String customer_name, String product_name, String product_version, String issue_summary, String postfix) {
        try {
            ApplicationDB db = new ApplicationDB();
            Connection con = db.getConnection();

            FileInputStream is = new FileInputStream(file);
//            String query = "INSERT INTO `ridge`.`issue` (`issue_detail`) VALUES (?)";
            String query = "INSERT INTO `ridge`.`issue` ( `customer_name`, `product_name`, `product_version`, `issue_summary`, `issue_detail`, `file_type`) " +
                    "VALUES (?,?,?,?,?,?)";
            PreparedStatement ptmt = con.prepareStatement(query);
            ptmt.setString(1,customer_name);
            ptmt.setString(2,product_name);
            ptmt.setString(3,product_version);
            ptmt.setString(4,issue_summary);
            ptmt.setBinaryStream(5, is,is.available());
            ptmt.setString(6,postfix);
            ptmt.execute();

        }catch (Exception e) {
           System.out.print(e);
        }
    }


}
