/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pack1;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.jws.Oneway;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Kushagr Jolly
 */
@WebService(serviceName = "test")
public class test {
    double area;
    private double OnepixArea;
    String returnVal;
    double leafArea0;
    double leafArea180;
    int choice=1;
    private int id;
    double Leaf_Area=0;

    
     @WebMethod(operationName = "convertStringtoImage")
    public String convertStringtoImage(@WebParam(name = "encodedImageStr") String encodedImageStr, @WebParam(name = "fileName") String fileName,@WebParam(name = "AOR") String AOR,@WebParam(name = "val") int value) throws SQLException, ClassNotFoundException{
        FileOutputStream imageOutFile = null; 
         try {
             Connection con,con1;
             Statement stmtnew = null;
             area=Double.parseDouble(AOR);
             //int val=Integer.parseInt(value);
             System.out.println("connecting");
             Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
             System.out.println("connected");
             con = DriverManager.getConnection("jdbc:odbc:test");
             System.out.println(" driver loaded in connection.jsp");
             stmtnew   = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
             
             
             // Decode String using Base64 Class
             byte[] imageByteArray = Base64.decodeBase64(encodedImageStr);
             // Write Image into File system - Make sure you update the path
             imageOutFile = new FileOutputStream("D:/IASRI/cropped/" + fileName);
             imageOutFile.write(imageByteArray);
             imageOutFile.close();
             System.out.println("Image Successfully Stored");
             FileInputStream leafPicPath= new FileInputStream("D:/IASRI/cropped/" + fileName);
             BufferedImage cat;
             int height,width;
             cat = ImageIO.read(leafPicPath);
             height = cat.getHeight();
             width = cat.getWidth();
             for (int w = 0; w < cat.getWidth(); w++) {
                 
                 for (int h = 0; h < cat.getHeight(); h++) {
                     // BufferedImage.getRGB() saves the colour of the pixel as a single integer.
                     // use Color(int) to grab the RGB values individually.
                     
                     Color color = new Color(cat.getRGB(w, h));
                     
                     // use the RGB values to get their average.
                     
                     int averageColor = ((color.getRed() + color.getGreen() + color.getBlue()) / 3);
// create a new Color object using the average colour as the red, green and blue
                     
                     // colour values
                     
                     Color avg = new Color(averageColor, averageColor, averageColor);
                     
                     
                     
                     // set the pixel at that position to the new Color object using Color.getRGB().				

                     cat.setRGB(w, h, avg.getRGB()); 

                 }
                 
             }String greyPicPath = "D:/IASRI/grey/" + fileName;
             greyPicPath = greyPicPath.trim();
             File outputfile = new File(greyPicPath);
             ImageIO.write(cat, "jpg", outputfile);
             System.out.println("Image is successfully converted to grayscale");
             

             String binPicPath = "D:/IASRI/bin/" + fileName;
             File f2 = new File(binPicPath);
             System.out.println("1");
             ImageProcessor ip;
             ImagePlus imp = new ImagePlus(greyPicPath);
             System.out.println("2");
             ip = imp.getProcessor();
             ip.invertLut();
             ip.autoThreshold();
             System.out.println("3");
             BufferedImage bf = ip.getBufferedImage();
             System.out.println("4");
             ImageIO.write(bf, "jpg", f2);
             System.out.println("Image is successfully converted to binary image");
             if(choice==1){
                 String Inserted1 = "insert into test (PhyAOR) values ('"+area+"')";
             System.out.println("InsertedQuery"+Inserted1);
             stmtnew.executeUpdate(Inserted1);
             Statement stmt = con.createStatement();
             ResultSet rs=null;
            String ID = "select MAX(id) as id from test";
            System.out.println("Query"+ID);
            rs = stmt.executeQuery(ID);
            while (rs.next()) {
                id = rs.getInt("id");
            }
            String Inserted2 = "update test set fileName0=? where id=?";
            PreparedStatement ps=con.prepareStatement(Inserted2);
            ps.setString(1, fileName);
            ps.setDouble(2, id);
            int rt=ps.executeUpdate();
            choice++;
             }
             System.out.println(choice);
             if(value==1){
                 
                int count=countblackpixel(binPicPath);
                calculatepixA(count, area);
                returnVal=value+"/"+count+"/"+OnepixArea;

             }
             else if(value==2){
                 
                int flag=countblackpixel(binPicPath);
               // calculatepixA(flag, area);
                 leafarea0(flag,area);
                 returnVal=value+"/"+flag+"/"+OnepixArea+"/"+leafArea0;
                 System.out.println(returnVal);
             }
             else if(value==3){
                 String Inserted3 = "update test set fileName180=? where id=?";
             PreparedStatement ps1=con.prepareStatement(Inserted3);
            ps1.setString(1, fileName);
            ps1.setDouble(2, id);
            int rt=ps1.executeUpdate();
                 int black=countblackpixel(binPicPath);
                 leafarea180(area, black);
                 finalarea();
                 returnVal=value+"/"+black+"/"+OnepixArea+"/"+leafArea180+"/"+Leaf_Area;
             }
             else if(value==4){
                 finalarea();
                 System.out.println("hello");
                 returnVal=value+"/"+Leaf_Area;
                 System.out.println(returnVal);

             }
         } catch (FileNotFoundException ex) {
             Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 imageOutFile.close();
             } catch (IOException ex) {
                 Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
           return returnVal;
		
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "calculatepixA")
    public void calculatepixA(@WebParam(name = "count") double count, @WebParam(name = "AOR") double AOR) throws ClassNotFoundException {
        
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
             System.out.println("connected");
             Connection con = DriverManager.getConnection("jdbc:odbc:test");
             System.out.println(" driver loaded in connection.jsp");
            OnepixArea=AOR/count;
            OnepixArea=Math.round(OnepixArea*10000.0)/10000.0;
            System.out.println(OnepixArea);
            String Inserted3 = "update test set onePixA=? where id=?";
            PreparedStatement ps1=con.prepareStatement(Inserted3);
            ps1.setDouble(1, OnepixArea);
            ps1.setInt(2, id);
            int rt1=ps1.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "countblackpixel")
    public int countblackpixel(@WebParam(name = "binPicPath") String binPicPath) {
        
             ImageProcessor ipB;
             ImagePlus impB = new ImagePlus(binPicPath);
             ipB=impB.getProcessor();
             int h = ipB.getHeight();
             int w = ipB.getWidth();
             int count = 0;
             for (int k = 0; k <= w; k++) {
                 
                 for (int l = 0; l < h; l++) {

                        if (ipB.getPixel(k, l) == 0) {
                            count = count + 1;
                        }

                    }
                }
             System.out.println("leaves black pixels=" + count);
        //TODO write your implementation code here:
        return count;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "leafarea0")
    
    public void leafarea0(@WebParam(name = "flag") double flag,@WebParam(name = "area") double area) throws SQLException {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Connection con = DriverManager.getConnection("jdbc:odbc:test");
            Statement stmt = con.createStatement();
            ResultSet rs = null;
            String OnePixA = "select onePixA from test where id=" + id + "";
            rs = stmt.executeQuery(OnePixA);
            double opa = 0;
            while (rs.next()) {
                opa = rs.getDouble("onePixA");
                opa=Math.round(opa*10000.0)/10000.0;
            }
            leafArea0 =flag *opa;
             leafArea0=Math.round(leafArea0*10000.0)/10000.0;
             String str="update test set leafArea0=? where id=?";
         PreparedStatement ps=con.prepareStatement(str);
        ps.setDouble(1, leafArea0);
         ps.setInt(2, id);
         
         int rt=ps.executeUpdate();
         System.out.println("Leaf area is uploaded to database");

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "leafarea180")
    public void leafarea180(@WebParam(name = "area") double AOR,@WebParam(name = "flag") double flag) throws SQLException {
        try {
            double onePixArea=0;
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            System.out.println("connected");
            Connection con = DriverManager.getConnection("jdbc:odbc:test");
            System.out.println(" driver loaded in connection.jsp");
            Statement stmt = con.createStatement();
            ResultSet rs = null;
            String OnePixA = "select onePixA from test where id='" + id + "'";
            rs = stmt.executeQuery(OnePixA);
            while (rs.next()) {
                onePixArea = rs.getDouble("onePixA");
                onePixArea=Math.round(onePixArea*10000.0)/10000.0;
            }
            leafArea180 =flag *onePixArea ;
             leafArea180=Math.round(leafArea180*10000.0)/10000.0;
             System.out.println(leafArea180);
             String str="update test set leafArea180=? where id=?";
         PreparedStatement ps=con.prepareStatement(str);
        ps.setDouble(1, leafArea180);
         ps.setInt(2, id);
         
         int rt=ps.executeUpdate();
         System.out.println("Leaf area is uploaded to database");

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void finalarea() {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            Connection con = DriverManager.getConnection("jdbc:odbc:test");
            Statement stmt = con.createStatement();
            ResultSet rs = null;
            String LA0 = "select leafarea0 from test where id=" + id + "";
            rs = stmt.executeQuery(LA0);
            double la0=0;
            while (rs.next()) {
                la0 = rs.getDouble("leafarea0");
            }
            String LA180 = "select leafarea180 from test where id=" + id + "";
            rs = stmt.executeQuery(LA180);
            double la180=0;
            while (rs.next()) {
                la180 = rs.getDouble("leafarea180");
            }
        Leaf_Area = 30.57+1.21*la0-0.6*la180;
        System.out.println(Leaf_Area);
        String str="update test set finalleafarea=? where id=?";
        PreparedStatement ps=con.prepareStatement(str);
        ps.setDouble(1, Leaf_Area);
         ps.setInt(2, id);
         int rt=ps.executeUpdate();
         System.out.println("Leaf area is uploaded to database");

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }   catch (ClassNotFoundException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
