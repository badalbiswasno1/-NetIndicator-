package com.gym.nutrition;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    HashMap<String, Food> db = new HashMap<>();
    Food selected = null;
    SharedPreferences prefs;
    String lang = "en";
    ArrayList<HistoryItem> historyList = new ArrayList<>();
    
    class Food {
        String name, emoji, tip;
        double cal, pro, fat, carbs;
        Food(String n, String e, double c, double p, double f, double cb, String t) {
            name = n; emoji = e; cal = c; pro = p; fat = f; carbs = cb; tip = t;
        }
    }
    
    class HistoryItem {
        String date, foodName;
        double totalCal, totalPro, totalFat, totalCarbs;
        HistoryItem(String d, String f, double c, double p, double fa, double cb) {
            date = d; foodName = f; totalCal = c; totalPro = p; totalFat = fa; totalCarbs = cb;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        
        prefs = getSharedPreferences("GymNutrition", MODE_PRIVATE);
        lang = prefs.getString("language", "en");
        
        loadHistory();
        initDB();
        initViews();
        updateLanguage();
        showDailyTotal();
    }
    
    void initDB() {
        db.put("egg", new Food(getString("Boiled Egg"), "🥚", 128, 12.3, 8.8, 0.2, "Best protein source! 90% absorption"));
        db.put("chicken", new Food(getString("Chicken Breast"), "🍗", 165, 31, 3.6, 0, "31g protein per 100g! Grill for less oil"));
        db.put("rice", new Food(getString("White Rice"), "🍚", 130, 2.7, 0.3, 28, "Post-workout energy! Glycogen refill"));
        db.put("fish", new Food(getString("Salmon Fish"), "🐟", 208, 20, 13, 0, "Omega-3 for muscle recovery!"));
        db.put("paneer", new Food(getString("Paneer"), "🧀", 265, 18, 21, 1.2, "Vegetarian protein! 18g per 100g"));
        db.put("oats", new Food(getString("Oats"), "🌾", 389, 16.9, 6.9, 66, "Complex carbs! Slow energy release"));
        db.put("banana", new Food(getString("Banana"), "🍌", 89, 1.1, 0.3, 23, "Pre-workout! Potassium prevents cramps"));
        db.put("milk", new Food(getString("Milk"), "🥛", 42, 3.4, 1, 5, "Casein protein! Slow release at night"));
        db.put("peanut", new Food(getString("Peanut Butter"), "🥜", 588, 25, 50, 20, "Healthy fats + protein! Bulking best"));
        db.put("whey", new Food(getString("Whey Protein"), "🥤", 400, 80, 5, 10, "Fast absorbing! Take within 30min post-workout"));
        db.put("potato", new Food(getString("Sweet Potato"), "🍠", 86, 1.6, 0.1, 20, "Complex carbs + fiber! Cutting best"));
        db.put("broccoli", new Food(getString("Broccoli"), "🥦", 34, 2.8, 0.4, 7, "Low cal high fiber! Cutting must"));
        db.put("almond", new Food(getString("Almonds"), "🌰", 579, 21, 50, 22, "Healthy fats + Vitamin E! Snack time"));
        db.put("yogurt", new Food(getString("Greek Yogurt"), "🍦", 59, 10, 0.4, 3.6, "Probiotic + protein! Gut health"));
        db.put("beef", new Food(getString("Beef"), "🥩", 250, 26, 17, 0, "Creatine + iron! Strength training"));
        db.put("roti", new Food(getString("Tandoori Roti"), "🫓", 299, 7.85, 9.2, 46.13, "Whole wheat! Better than naan"));
        db.put("dal", new Food(getString("Dal/Lentils"), "🥣", 116, 9, 0.4, 20, "Plant protein! Daily staple"));
        db.put("sabji", new Food(getString("Mixed Sabji"), "🥗", 65, 2.5, 3, 8, "Vitamins + fiber! Eat daily"));
        db.put("soya", new Food(getString("Soya Chunks"), "🫘", 336, 52, 0.5, 33, "Highest plant protein! 52g per 100g"));
        db.put("tandoori", new Food(getString("Tandoori Chicken"), "🍗", 273, 30, 14, 5, "Low fat! Tandoor cooking reduces oil"));
        db.put("chana", new Food(getString("Chana/Chickpeas"), "🫘", 164, 8.9, 2.6, 27, "Fiber + protein! Satiety king"));
        db.put("curd", new Food(getString("Curd/Yogurt"), "🥣", 98, 11, 4.3, 3.4, "Probiotic! Digestion helper"));
        db.put("ghee", new Food(getString("Ghee"), "🧈", 900, 0, 100, 0, "Healthy fats! Bulletproof coffee"));
        db.put("bhat", new Food(getString("Bhat/Rice"), "🍚", 130, 2.7, 0.3, 28, "Bengali staple! Lunch essential"));
        db.put("mach", new Food(getString("Ilish/Hilsa"), "🐟", 220, 22, 14, 0, "Bengali favorite! Omega-3 rich"));
        db.put("chingri", new Food(getString("Chingri/Prawn"), "🦐", 106, 20, 1.7, 0, "Low fat protein! Bengali delicacy"));
        db.put("mutton", new Food(getString("Mutton/Khashi"), "🍖", 294, 25, 21, 0, "Bengali special! Iron rich"));
        db.put("luchi", new Food(getString("Luchi/Puri"), "🫓", 325, 6, 15, 42, "Sunday special! Deep fried"));
        db.put("alurdom", new Food(getString("Alur Dom"), "🥔", 180, 4, 8, 25, "Bengali snack! Spicy potato"));
        db.put("singara", new Food(getString("Singara/Samosa"), "🥟", 260, 4, 15, 28, "Evening snack! Crispy"));
        db.put("mishti", new Food(getString("Mishti/Sweets"), "🍬", 350, 6, 15, 50, "Bengali sweet! Occasional treat"));
        db.put("shukto", new Food(getString("Shukto"), "🥗", 85, 3, 4, 10, "Bengali starter! Bitter veg mix"));
        db.put("macherjhol", new Food(getString("Macher Jhol"), "🍲", 150, 18, 6, 5, "Bengali fish curry! Light and tasty"));
        db.put("kosha", new Food(getString("Mangsho Kosha"), "🍖", 320, 28, 22, 3, "Bengali mutton curry! Spicy"));
    }
    
    String getString(String en) {
        if (lang.equals("bn")) {
            switch(en) {
                case "Boiled Egg": return "সিদ্ধ ডিম";
                case "Chicken Breast": return "চিকেন ব্রেস্ট";
                case "White Rice": return "সাদা ভাত";
                case "Salmon Fish": return "স্যালমন মাছ";
                case "Paneer": return "পনির";
                case "Oats": return "ওটস";
                case "Banana": return "কলা";
                case "Milk": return "দুধ";
                case "Peanut Butter": return "পিনাট বাটার";
                case "Whey Protein": return "হুয়ে প্রোটিন";
                case "Sweet Potato": return "মিষ্টি আলু";
                case "Broccoli": return "ব্রোকলি";
                case "Almonds": return "বাদাম";
                case "Greek Yogurt": return "গ্রিক দই";
                case "Beef": return "গরুর মাংস";
                case "Tandoori Roti": return "তন্দুরি রুটি";
                case "Dal/Lentils": return "ডাল";
                case "Mixed Sabji": return "মিক্সড সবজি";
                case "Soya Chunks": return "সয়া চাংকস";
                case "Tandoori Chicken": return "তন্দুরি চিকেন";
                case "Chana/Chickpeas": return "ছোলা";
                case "Curd/Yogurt": return "দই";
                case "Ghee": return "ঘি";
                case "Bhat/Rice": return "ভাত";
                case "Ilish/Hilsa": return "ইলিশ মাছ";
                case "Chingri/Prawn": return "চিংড়ি";
                case "Mutton/Khashi": return "খাসির মাংস";
                case "Luchi/Puri": return "লুচি";
                case "Alur Dom": return "আলুর দম";
                case "Singara/Samosa": return "সিঙ্গারা";
                case "Mishti/Sweets": return "মিষ্টি";
                case "Shukto": return "শুক্তো";
                case "Macher Jhol": return "মাছের ঝোল";
                case "Mangsho Kosha": return "মাংস কষা";
                default: return en;
            }
        } else if (lang.equals("hi")) {
            switch(en) {
                case "Boiled Egg": return "उबला अंडा";
                case "Chicken Breast": return "चिकन ब्रेस्ट";
                case "White Rice": return "सफेद चावल";
                case "Salmon Fish": return "सैलमन मछली";
                case "Paneer": return "पनीर";
                case "Oats": return "ओट्स";
                case "Banana": return "केला";
                case "Milk": return "दूध";
                case "Peanut Butter": return "मूंगफली बटर";
                case "Whey Protein": return "व्हे प्रोटीन";
                case "Sweet Potato": return "शकरकंदी";
                case "Broccoli": return "ब्रोकोली";
                case "Almonds": return "बादाम";
                case "Greek Yogurt": return "ग्रीक दही";
                case "Beef": return "बीफ";
                case "Tandoori Roti": return "तंदूरी रोटी";
                case "Dal/Lentils": return "दाल";
                case "Mixed Sabji": return "मिक्स सब्जी";
                case "Soya Chunks": return "सोया चंक्स";
                case "Tandoori Chicken": return "तंदूरी चिकन";
                case "Chana/Chickpeas": return "छोले";
                case "Curd/Yogurt": return "दही";
                case "Ghee": return "घी";
                case "Bhat/Rice": return "भात";
                case "Ilish/Hilsa": return "इलिश मछली";
                case "Chingri/Prawn": return "चिंगड़ी";
                case "Mutton/Khashi": return "खासी का मांस";
                case "Luchi/Puri": return "लुची";
                case "Alur Dom": return "आलूर दम";
                case "Singara/Samosa": return "सिंगाड़ा";
                case "Mishti/Sweets": return "मिष्टी";
                case "Shukto": return "शुक्तो";
                case "Macher Jhol": return "माछेर झोल";
                case "Mangsho Kosha": return "मांगशो कोशा";
                default: return en;
            }
        }
        return en;
    }
    
    void initViews() {
        String[] items = new String[db.size()];
        int i = 0;
        for (Food f : db.values()) {
            items[i++] = f.emoji + " " + f.name;
        }
        
        Spinner sp = findViewById(R.id.spinnerFood);
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items));
        
        findViewById(R.id.btnCalc).setOnClickListener(v -> {
            int pos = sp.getSelectedItemPosition();
            String[] keys = db.keySet().toArray(new String[0]);
            Food f = db.get(keys[pos]);
            double w = Double.parseDouble(((EditText)findViewById(R.id.etWeight)).getText().toString().isEmpty() ? "100" : ((EditText)findViewById(R.id.etWeight)).getText().toString());
            int q = Integer.parseInt(((EditText)findViewById(R.id.etQty)).getText().toString().isEmpty() ? "1" : ((EditText)findViewById(R.id.etQty)).getText().toString());
            double r = w / 100;
            double tc = f.cal*r*q, tp = f.pro*r*q, tf = f.fat*r*q, tcb = f.carbs*r*q;
            show(f.name, f.emoji, tc, tp, tf, tcb, f.tip);
            addToHistory(f.name, tc, tp, tf, tcb);
        });
        
        findViewById(R.id.btnSearch).setOnClickListener(v -> searchOnline());
        findViewById(R.id.btnSettings).setOnClickListener(v -> showSettings());
        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());
    }
    
    void addToHistory(String name, double cal, double pro, double fat, double carbs) {
        String today = java.text.SimpleDateFormat.getDateInstance().format(new java.util.Date());
        historyList.add(new HistoryItem(today, name, cal, pro, fat, carbs));
        saveHistory();
        showDailyTotal();
    }
    
    void saveHistory() {
        StringBuilder sb = new StringBuilder();
        for (HistoryItem h : historyList) {
            sb.append(h.date).append("|").append(h.foodName).append("|")
              .append(h.totalCal).append("|").append(h.totalPro).append("|")
              .append(h.totalFat).append("|").append(h.totalCarbs).append("\n");
        }
        prefs.edit().putString("history", sb.toString()).apply();
    }
    
    void loadHistory() {
        String data = prefs.getString("history", "");
        if (data.isEmpty()) return;
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|");
            if (parts.length == 6) {
                historyList.add(new HistoryItem(parts[0], parts[1], 
                    Double.parseDouble(parts[2]), Double.parseDouble(parts[3]),
                    Double.parseDouble(parts[4]), Double.parseDouble(parts[5])));
            }
        }
    }
    
    void showDailyTotal() {
        String today = java.text.SimpleDateFormat.getDateInstance().format(new java.util.Date());
        double totalCal = 0, totalPro = 0;
        for (HistoryItem h : historyList) {
            if (h.date.equals(today)) {
                totalCal += h.totalCal;
                totalPro += h.totalPro;
            }
        }
        TextView tvDaily = findViewById(R.id.tvDailyTotal);
        if (tvDaily != null) {
            tvDaily.setText(String.format("📅 Today: 🔥 %.0f kcal | 💪 %.1f g protein", totalCal, totalPro));
        }
    }
    
    void showHistory() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);
        layout.setBackgroundColor(0xFF0a0a14);
        
        TextView title = new TextView(this);
        title.setText("📊 Last 14 Days History");
        title.setTextColor(0xFFff4757);
        title.setTextSize(20);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);
        
        HashMap<String, Double> dailyCal = new HashMap<>();
        HashMap<String, Double> dailyPro = new HashMap<>();
        
        for (HistoryItem h : historyList) {
            dailyCal.put(h.date, dailyCal.getOrDefault(h.date, 0.0) + h.totalCal);
            dailyPro.put(h.date, dailyPro.getOrDefault(h.date, 0.0) + h.totalPro);
        }
        
        ArrayList<String> dates = new ArrayList<>(dailyCal.keySet());
        for (int i = dates.size() - 1; i >= 0 && i >= dates.size() - 14; i--) {
            String d = dates.get(i);
            TextView tv = new TextView(this);
            tv.setText(String.format("📅 %s\n🔥 %.0f kcal | 💪 %.1f g protein", d, dailyCal.get(d), dailyPro.get(d)));
            tv.setTextColor(0xFFFFFFFF);
            tv.setPadding(10, 15, 10, 15);
            tv.setBackgroundColor(0xFF1a1a2e);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 8, 0, 8);
            tv.setLayoutParams(lp);
            layout.addView(tv);
        }
        
        ScrollView sv = new ScrollView(this);
        sv.addView(layout);
        new AlertDialog.Builder(this).setView(sv).setPositiveButton("OK", null).show();
    }
    
    void searchOnline() {
        String q = ((EditText)findViewById(R.id.etSearch)).getText().toString().trim();
        if(q.isEmpty()) return;
        try {
            HttpURLConnection c = (HttpURLConnection)new URL("https://api.nal.usda.gov/fdc/v1/foods/search?api_key=DEMO_KEY&query="+q+"&pageSize=5").openConnection();
            Scanner s = new Scanner(c.getInputStream());
            StringBuilder b = new StringBuilder();
            while(s.hasNext()) b.append(s.nextLine());
            JSONArray foods = new JSONObject(b.toString()).getJSONArray("foods");
            
            LinearLayout resultsContainer = findViewById(R.id.searchResultsContainer);
            resultsContainer.removeAllViews();
            
            for(int i=0;i<foods.length();i++) {
                JSONObject f = foods.getJSONObject(i);
                double cal=0,pro=0,fat=0,carbs=0;
                for(int j=0;j<f.getJSONArray("foodNutrients").length();j++) {
                    JSONObject n = f.getJSONArray("foodNutrients").getJSONObject(j);
                    String nn = n.getString("nutrientName");
                    if(nn.equals("Energy")) cal=n.getDouble("value");
                    else if(nn.equals("Protein")) pro=n.getDouble("value");
                    else if(nn.contains("fat")) fat=n.getDouble("value");
                    else if(nn.contains("Carbohydrate")) carbs=n.getDouble("value");
                }
                
                Button btn = new Button(this);
                btn.setText(f.getString("description") + "\n🔥 " + Math.round(cal) + " kcal | 💪 " + pro + "g | per 100g");
                btn.setBackgroundColor(0xFF1a1a2e);
                btn.setTextColor(0xFFFFFFFF);
                btn.setPadding(20, 20, 20, 20);
                btn.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                ((LinearLayout.LayoutParams)btn.getLayoutParams()).setMargins(0, 8, 0, 8);
                
                final double fcal=cal, fpro=pro, ffat=fat, fcarbs=carbs;
                final String fname = f.getString("description");
                btn.setOnClickListener(v -> {
                    selected = new Food(fname, "🌐", fcal, fpro, ffat, fcarbs, "Online data from USDA");
                    double w = Double.parseDouble(((EditText)findViewById(R.id.etWeight)).getText().toString().isEmpty() ? "100" : ((EditText)findViewById(R.id.etWeight)).getText().toString());
                    int qt = Integer.parseInt(((EditText)findViewById(R.id.etQty)).getText().toString().isEmpty() ? "1" : ((EditText)findViewById(R.id.etQty)).getText().toString());
                    double r = w / 100;
                    show(selected.name, selected.emoji, selected.cal*r*qt, selected.pro*r*qt, selected.fat*r*qt, selected.carbs*r*qt, selected.tip);
                });
                
                resultsContainer.addView(btn);
            }
        } catch(Exception e) {
            Toast.makeText(this, "Internet error! Use offline mode.", Toast.LENGTH_LONG).show();
        }
    }
    
    void show(String n, String e, double c, double p, double f, double cb, String tip) {
        findViewById(R.id.layoutResults).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.tvFoodName)).setText(e + " " + n);
        ((TextView)findViewById(R.id.tvCalories)).setText(String.format("%.1f", c));
        ((TextView)findViewById(R.id.tvProtein)).setText(String.format("%.1f g", p));
        ((TextView)findViewById(R.id.tvFat)).setText(String.format("%.1f g", f));
        ((TextView)findViewById(R.id.tvCarbs)).setText(String.format("%.1f g", cb));
        ((TextView)findViewById(R.id.tvTip)).setText("💡 " + tip);
        
        int proteinPct = Math.min((int)((p/150)*100), 100);
        int calPct = Math.min((int)((c/2500)*100), 100);
        
        ((ProgressBar)findViewById(R.id.proteinBar)).setProgress(proteinPct);
        ((ProgressBar)findViewById(R.id.calBar)).setProgress(calPct);
        ((TextView)findViewById(R.id.tvProteinPct)).setText(proteinPct + "%");
        ((TextView)findViewById(R.id.tvCalPct)).setText(calPct + "%");
    }
    
    void showSettings() {
        String[] langs = {"English", "বাংলা (Bengali)", "हिंदी (Hindi)"};
        new AlertDialog.Builder(this)
            .setTitle("🌐 Select Language")
            .setItems(langs, (dialog, which) -> {
                String[] codes = {"en", "bn", "hi"};
                lang = codes[which];
                prefs.edit().putString("language", lang).apply();
                recreate();
            })
            .show();
    }
    
    void updateLanguage() {
        if (lang.equals("bn")) {
            ((TextView)findViewById(R.id.tvTitle)).setText("💪 জিম নিউট্রিশন");
            ((EditText)findViewById(R.id.etWeight)).setHint("⚖️ ওজন (গ্রাম)");
            ((EditText)findViewById(R.id.etQty)).setHint("🔢 পরিমাণ");
            ((Button)findViewById(R.id.btnCalc)).setText("📹 হিসাব করুন");
            ((EditText)findViewById(R.id.etSearch)).setHint("🔍 অনলাইনে খুঁজুন...");
            ((Button)findViewById(R.id.btnSearch)).setText("🔍 অনলাইন সার্চ");
            ((TextView)findViewById(R.id.tvOfflineLabel)).setText("📴 অফলাইন ফুড (34টি)");
            ((TextView)findViewById(R.id.tvOnlineLabel)).setText("🌐 অনলাইন সার্চ");
        } else if (lang.equals("hi")) {
            ((TextView)findViewById(R.id.tvTitle)).setText("💪 जिम न्यूट्रिशन");
            ((EditText)findViewById(R.id.etWeight)).setHint("⚖️ वजन (ग्राम)");
            ((EditText)findViewById(R.id.etQty)).setHint("🔢 मात्रा");
            ((Button)findViewById(R.id.btnCalc)).setText("📹 गणना करें");
            ((EditText)findViewById(R.id.etSearch)).setHint("🔍 ऑनलाइन खोजें...");
            ((Button)findViewById(R.id.btnSearch)).setText("🔍 ऑनलाइन सर्च");
            ((TextView)findViewById(R.id.tvOfflineLabel)).setText("📴 ऑफलाइन फूड (34)");
            ((TextView)findViewById(R.id.tvOnlineLabel)).setText("🌐 ऑनलाइन सर्च");
        }
    }
}
