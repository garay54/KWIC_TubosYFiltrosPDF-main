package filtrospdf;

import filters.Filter;
import filters.Pipe;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class BuscadorPalabrasPDF extends Filter {

    private String archivo;

    public BuscadorPalabrasPDF(Pipe in, Pipe out, String archivo){
        super(in, out);
        //RUTA PDF
        this.archivo = archivo;
    }

    @Override
    public void transform(){
        try {
            //RECIBE PALABRAS A BUSCAR
            String[] lineas = input.read().trim().split("\n");
            HashSet<String> wordspages = find(lineas);
            for (String palabraClave: wordspages) {
                output.write(palabraClave + "\n");
            }
            output.closeWriter();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private HashSet<String> find(String[] lineas) {
        HashSet<String> wordsPages = new HashSet<>();
        try {
            //GUARDAMOS NUESTRO PDF EN DOCUMENT
            PDDocument document = PDDocument.load(new File(archivo));
            //CREAMO SUN OBJETO PARA GUARDAR TOODO EL TEXTO
            PDFTextStripper reader = new PDFTextStripper();
            //PARA GUARDAR EL TEXTO DE UNA PAGINA
            String pageText;
            for (String item : lineas) {
                //wordsformat se usa para concatenar las palabras y las paginas a la palabra
                item=item.toLowerCase();
                if(!wordsPages.contains(item)) {
                    StringBuilder wordsFormat = new StringBuilder(item);
                    wordsFormat.append(", pages: [");
                    for (int i = 1; i <= document.getNumberOfPages(); i++) {
                        //RECORREMOS DESDE LA PIMER PALABRA HASTA LA ULTIMA DE CADA PAGINA
                        reader.setStartPage(i);
                        reader.setEndPage(i);
                        //GUARDAMOS TODAS LAS PALABRAS DE CADA PAGINA EN PAGETEXT
                        pageText = reader.getText(document);
                        if (pageText != null) {
                            pageText = pageText.replaceAll("\r\n", " ").toLowerCase();
                            if (pageText.contains(item) ) {
                                wordsFormat.append(i);
                                wordsFormat.append(", ");
                            }
                        }
                    }
                    wordsFormat.delete(wordsFormat.length() - 2, wordsFormat.length());
                    wordsFormat.append("]");
                    wordsPages.add(wordsFormat.toString());
                }
            }
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("KWIC Error: No se encontro el archivo a leer.");
        }
        return wordsPages;
    }
}
