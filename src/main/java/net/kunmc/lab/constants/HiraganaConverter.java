package net.kunmc.lab.constants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public final class HiraganaConverter {

    private static HashMap<String,String> convertMap = new HashMap<String, String>();

    public HiraganaConverter(){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(getResource("convertTable.csv"), "UTF-8"))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] str = line.split(",");
                convertMap.put(str[0], str[1]);
            }
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    };

    public static String convertText(String text) {
        convertMap.forEach((key, value) -> {
            if(!text.contains(key)) {
                return;
            }
            text.replaceAll(text, value);
        });
        return text;
    }

    private static InputStream getResource(String path) {
        return HiraganaConverter.class.getClassLoader().getResourceAsStream(path);
    }
}
