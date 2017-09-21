package com.referendum.uoctubre.utils;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.main.Constants;
import com.referendum.uoctubre.model.ColegiElectoral;
import com.referendum.uoctubre.model.PollingStationResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PollingStationDataFetcher {

    public static PollingStationResponse getUserPollingStation(String nif, Date birthDate, int zipCode) {
        PollingStationResponse pollingStationResponse = new PollingStationResponse();
        String keyDate = new SimpleDateFormat("yyyyMMdd", Locale.US).format(birthDate);
        String keyZipCode = String.format(Locale.US, "%05d", zipCode);
        String keyNif = nif.substring(nif.length() - 6, nif.length());

        String key = keyNif + keyDate + keyZipCode;

        String firstSha256 = hash(bucleHash(key));
        String secondSha256 = hash(firstSha256);
        String dir = secondSha256.substring(0, 2);
        String file = secondSha256.substring(2, 4);

        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        String api = firebaseRemoteConfig.getString(Constants.FIREBASE_CONFIG_API_URL);

        try {
            URL url = new URL(api + dir + "/" + file + ".db");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder resultStringBuilder = new StringBuilder();
            String str;
            while ((str = in.readLine()) != null) {
                if (resultStringBuilder.length() != 0) {
                    resultStringBuilder.append("\n");
                }
                resultStringBuilder.append(str);
            }
            in.close();

            String[] lines = resultStringBuilder.toString().split("\n");

            String result = null;

            for (String line : lines) {
                if (line.substring(0, 60).equals(secondSha256.substring(4))) {
                    result = decrypt(line.substring(60), firstSha256);
                }
            }

            if (result != null) {
                String[] info = result.split("#");

                ColegiElectoral colegiElectoral = new ColegiElectoral();
                colegiElectoral.setLocal(info[0]);
                colegiElectoral.setAdresa(info[1]);
                colegiElectoral.setMunicipi(info[2]);
                colegiElectoral.setDistricte(info[3]);
                colegiElectoral.setSeccio(info[4]);
                colegiElectoral.setMesa(info[5]);

                pollingStationResponse.setStatus("ok");
                pollingStationResponse.setPollingStation(colegiElectoral);
            } else {
                pollingStationResponse.setStatus("not_found");
                pollingStationResponse.setPollingStation(null);
            }
        } catch (Exception e) {
            pollingStationResponse.setStatus("error");
            pollingStationResponse.setPollingStation(null);
        }

        return pollingStationResponse;
    }

    private static String bucleHash(String clau) {
        String clauTemp = clau;
        for (int i = 0; i < 1714 /* Patriotisme per sobre de tot */; i++) {
            clauTemp = hash(clauTemp);
        }
        return clauTemp;
    }

    private static String hash(String text) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(Charset.forName("UTF-8")));

            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("no algo");
        }
    }

    //Taken from: https://stackoverflow.com/a/9855338/1254846
    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //Taken from: https://stackoverflow.com/a/140861/1254846
    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    //Taken from (and adapted): https://stackoverflow.com/a/11786924/1254846
    private static String decrypt(String text, String password) {
        String decrypted;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            final byte[][] keyAndIV = EVP_BytesToKey(
                    32,
                    cipher.getBlockSize(),
                    MessageDigest.getInstance("MD5"),
                    null,
                    password.getBytes(Charset.forName("UTF-8")),
                    1);
            SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
            IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);

            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            decrypted = new String(cipher.doFinal(hexToBytes(text)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return decrypted;
    }

    //Taken from https://stackoverflow.com/a/11786924/1254846
    private static byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md,
                                           byte[] salt, byte[] data, int count) {
        byte[][] both = new byte[2][];
        byte[] key = new byte[key_len];
        int key_ix = 0;
        byte[] iv = new byte[iv_len];
        int iv_ix = 0;
        both[0] = key;
        both[1] = iv;
        byte[] md_buf = null;
        int nkey = key_len;
        int niv = iv_len;
        int i = 0;
        if (data == null) {
            return both;
        }
        int addmd = 0;
        for (; ; ) {
            md.reset();
            if (addmd++ > 0) {
                md.update(md_buf);
            }
            md.update(data);
            if (null != salt) {
                md.update(salt, 0, 8);
            }
            md_buf = md.digest();
            for (i = 1; i < count; i++) {
                md.reset();
                md.update(md_buf);
                md_buf = md.digest();
            }
            i = 0;
            if (nkey > 0) {
                for (; ; ) {
                    if (nkey == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    key[key_ix++] = md_buf[i];
                    nkey--;
                    i++;
                }
            }
            if (niv > 0 && i != md_buf.length) {
                for (; ; ) {
                    if (niv == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    iv[iv_ix++] = md_buf[i];
                    niv--;
                    i++;
                }
            }
            if (nkey == 0 && niv == 0) {
                break;
            }
        }
        for (i = 0; i < md_buf.length; i++) {
            md_buf[i] = 0;
        }
        return both;
    }
}
