package com.util;

import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.apps.rasterizer.SVGConverterException;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.print.Doc;
import javax.xml.crypto.dsig.Transform;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rensong.pu on 2016/9/12.
 */
public class SvgPngConvert {
    private static Logger logger = LoggerFactory.getLogger(SvgPngConvert.class);

    public static Document createDocument(InputStream is) {
        Document doc = null;
        //创建一个svg文档
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory sdf = new SAXSVGDocumentFactory(parser);
        try {
            doc = sdf.createSVGDocument(null, is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    public File saveSvg(InputStream svgStream) {
        Document svgXmlDoc = createDocument(svgStream);
        FileOutputStream fos = null;
        File file = null;
        try {
            file = File.createTempFile("svgFile", ".svg");
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource domSource = new DOMSource(svgXmlDoc);
            fos = new FileOutputStream(file);
            transformer.transform(domSource, new StreamResult(fos));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    //处理svg文件转png文件
    public static void convert(String imgPath, String svgCode) {
        File svgFile = new File(svgCode);
        //convert svg to Png
        SVGConverter converter = new SVGConverter();
        converter.setDestinationType(DestinationType.PNG);
        converter.setSources(new String[]{svgFile.toString()});
        converter.setDst(new File(imgPath));
        try {
            converter.execute();
        } catch (SVGConverterException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对流入的svg字符串做处理
     *
     * @param svgCode
     * @return
     */
    public static String workXml(String svgCode) {
        String res = null;
        try {
            InputStream is = new ByteArrayInputStream(svgCode.getBytes("utf-8"));
            XmlUtil xmlUtil = new XmlUtil(is);
            xmlUtil.buildDoc();
            Document document = xmlUtil.getDocument();
            Node root = document.getFirstChild();
            xmlUtil.rgbWork(root);//rgb处理
            xmlUtil.clipPathNone(root);//处理url#
            List<String> textVal = new ArrayList<>(); //图例列表
            textVal.add("新访客-UV");
            textVal.add("老访客-UV");
            xmlUtil.legendWork(root, textVal);
            xmlUtil.saveToXml(document); //保存处理过的svg文件
            res = docToXml(document);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
    }

    //document转xml字符串
    public static String docToXml(Document document) {
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty("encoding", "utf-8");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(document), new StreamResult(bos));
            return bos.toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * svg字符串转png文件
     *
     * @param svgCode
     * @param pngPath
     */
    public static void convertToPng(String svgCode, String pngPath) {
        File file = new File(pngPath);
        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            convertToPng(svgCode, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * svgcode转为png文件，直接输出到流中
     *
     * @param svgCode
     * @param os
     */
    public static void convertToPng(String svgCode, OutputStream os) {
        try {
            byte[] bytes = svgCode.getBytes("utf-8");
            PNGTranscoder pt = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(bytes));
            TranscoderOutput output = new TranscoderOutput(os);
            pt.transcode(input, output);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (TranscoderException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static String modify(String orignSvg) {
        Pattern pattern = Pattern.compile("[a-zA-Z]+=\\\"(?>rgba.+?\\))");//正则匹配rgba的值
        Matcher matcher = pattern.matcher(orignSvg);
        while (matcher.matches()) {
            String ma = matcher.group();
            String[] rgba = ma.split(",");
            String opacity = rgba[3];
            String target = ma.substring(0, ma.indexOf(",", 2));
            String attriName = ma.substring(0, ma.indexOf("="));
            target = target + ") " + attriName + "-opacity=\"" + opacity + "\"";
            orignSvg.replaceAll(ma, target);
        }
        return orignSvg;
    }

    public static void main(String[] args) {
        File file = new File("E://svgdemo2.svg");
        String svgCode = null;
        try (InputStream is = new FileInputStream(file)) {
            BufferedReader bf = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = bf.readLine()) != null) {
                sb.append(line);
            }
            svgCode = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(svgCode); //输入内容svgCode
        convertToPng(workXml(svgCode), "E://svgout2.png");


    }

}
