package com.google.light.closureutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class ClosureCompiler {

    private String outputFile = null;
    private String jsCode = null;
    private String compilationLevel = null;
    private String outputFormat = null;

    public ClosureCompiler(String outputFile, String jsCode,
            String compilationlevel, String outputFormat) {
        this.outputFile = outputFile;
        this.jsCode = jsCode;
        this.compilationLevel = compilationlevel;
        this.outputFormat = outputFormat;
    }

    public void processCompilation() {
        try {
            String obfuscatedCode = sendForCompilation();
            saveObfuscatedCodeToFile(outputFile, obfuscatedCode);
        } catch (Exception e) {
            System.err.println("Exception when obfuscating code: "
                    + e.getMessage());
        }
    }

    private String sendForCompilation() throws Exception {
        URL url = new URL("http://closure-compiler.appspot.com/compile");

        String data = URLEncoder.encode("js_code", "UTF-8") + "="
                + URLEncoder.encode(jsCode, "UTF-8");
        data += "&" + URLEncoder.encode("compilation_level", "UTF-8") + "="
                + URLEncoder.encode(compilationLevel, "UTF-8");
        data += "&" + URLEncoder.encode("output_format", "UTF-8") + "="
                + URLEncoder.encode(outputFormat, "UTF-8");
        data += "&" + URLEncoder.encode("output_info", "UTF-8") + "="
                + URLEncoder.encode("compiled_code", "UTF-8");

        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String codeLine;

        StringBuffer obfuscatedCodeLine = new StringBuffer();
        while ((codeLine = rd.readLine()) != null) {
            obfuscatedCodeLine.append(codeLine).append(
                    System.getProperty("line.separator"));
        }
        wr.close();
        rd.close();

        return obfuscatedCodeLine.toString();
    }

    private void saveObfuscatedCodeToFile(String outputdirectory,
            String javascriptCode) throws Exception {
        FileWriter fstream = new FileWriter(outputdirectory);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(javascriptCode);
        // Close the output stream
        out.close();
    }

}