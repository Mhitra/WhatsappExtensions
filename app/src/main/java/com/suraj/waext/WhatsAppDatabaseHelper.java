package com.suraj.waext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by suraj on 2/12/16.
 */
public class WhatsAppDatabaseHelper {

    public static HashMap<String, Object> getNumberToNameHashMap() {
        return getContactsHashMap(true);
    }

    public static HashMap<String, Object> getNameToNumberHashMap() {
        return getContactsHashMap(false);
    }


    private static String[] execSQL(String dbName, String query) {
        Process process = null;
        Runtime runtime = Runtime.getRuntime();
        OutputStreamWriter outputStreamWriter;

        try {

            String command = "/data/data/com.whatsapp/databases/" + dbName + " " + "'" + query + "'" + ";";
            process = runtime.exec("su");

            outputStreamWriter = new OutputStreamWriter(process.getOutputStream());

            outputStreamWriter.write("sqlite3 " + command);

            outputStreamWriter.flush();
            outputStreamWriter.close();
            outputStreamWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String s;
            StringBuilder op = new StringBuilder();

            while ((s = bufferedReader.readLine()) != null) {
                op.append(s).append("\n");
            }

            return op.toString().split("\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;

    }

    private static HashMap<String, Object> getContactsHashMap(boolean swap) {
        HashMap<String, Object> hashMap = new HashMap<>();

        String[] arr = execSQL("wa.db", "Select display_name, jid FROM wa_contacts WHERE is_whatsapp_user=1");

        if(arr==null)
            return null;

        for (String contact : arr) {
            String potential[] = contact.split("\\|");

            if (potential.length < 2)
                continue;

            if (swap)        // swap = true -> number to name hashmap
                hashMap.put(potential[1].split("@")[0], potential[0]);
            else {            // swap = true -> name to number hashmap
                Object value = hashMap.get(potential[0]);

                if (value != null) {
                    List<String> numbers = new ArrayList<>();

                    if (value instanceof String) {
                        numbers.add(value.toString());
                        numbers.add(potential[1].split("@")[0]);
                    } else if (value instanceof List) {
                        numbers = (List<String>) value;
                        numbers.add(potential[1].split("@")[0]);
                    }
                    hashMap.put(potential[0], numbers);
                } else {
                    hashMap.put(potential[0], potential[1].split("@")[0]);
                }
            }
        }
        return hashMap;
    }

}