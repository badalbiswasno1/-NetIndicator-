package com.gym.nutrition;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    HashMap<String, Food> db = new HashMap<>();
    Food selected = null;
    
    class Food {
        String name, emoji, tip;
        double cal, pro, fat, carbs;
        Food(String n, String e, double c, double p, double f, double cb, String t) {
            name = n; emoji = e; cal = c; pro = p; fat = f; carbs = cb; tip = t;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        
        db.put("egg", new Food("Boiled Egg", "🥚", 128, 12.3, 8.8, 0.2, "Boiled egg best! Raw egg er protein 50% absorb hoy, boiled te 90%+"));
        db.put("chicken", new Food("Chicken Breast", "🍗", 165, 31, 3.6, 0, "Chicken breast = best protein! 100g te 31g protein"));
        db.put("rice", new Food("White Rice", "🍚", 130, 2.7, 0.3, 28, "Rice = energy source. Post-workout khao for glycogen refill"));
        db.put("fish", new Food("Salmon Fish", "🐟", 208, 20, 13, 0, "Fish e Omega-3 thake - muscle recovery er jonno best!"));
        db.put("paneer", new Food("Paneer", "🧀", 265, 18, 21, 1.2, "Paneer = vegetarian protein! 100g te 18g protein"));
        db.put("oats", new Food("Oats", "🌾", 389, 16.9, 6.9, 66, "Oats = complex carbs. Breakfast e ideal, slow energy release"));
        db.put("banana", new Food("Banana", "🍌", 89, 1.1, 0.3, 23, "Banana = pre-workout energy! Potassium thake cramp prevent kore"));
        db.put("milk", new Food("Milk", "🥛", 42, 3.4, 1, 5, "Milk = casein protein! Raat e khao slow release"));
        db.put("peanut", new Food("Peanut Butter", "🥜", 588, 25, 50, 20, "Peanut butter = healthy fat + protein! Bulking e best"));
        db.put("whey", new Food("Whey Protein", "🥤", 400, 80, 5, 10, "Whey = fast absorbing! Post-workout 30 min e khao"));
        db.put("potato", new Food("Sweet Potato", "🍠", 86, 1.6, 0.1, 20, "Sweet potato = complex carbs + fiber! Cutting e best"));
        db.put("broccoli", new Food("Broccoli", "🥦", 34, 2.8, 0.4, 7, "Broccoli = low cal high fiber! Cutting e must"));
        db.put("almond", new Food("Almonds", "🌰", 579, 21, 50, 22, "Almonds = healthy fat + vitamin E! Snack e khao"));
        db.put("yogurt", new Food("Greek Yogurt", "🍦", 59, 10, 0.4, 3.6, "Greek yogurt = probiotic + protein! Gut health er jonno"));
        db.put("beef", new Food("Beef", "🥩", 250, 26, 17, 0, "Beef = creatine + iron! Strength training er jonno best"));
        
        String[] items = {"🥚 Egg", "🍗 Chicken", "🍚 Rice", "🐟 Fish", "🧀 Paneer", "🌾 Oats", "🍌 Banana", "🥛 Milk", "🥜 Peanut Butter", "🥤 Whey", "🍠 Sweet Potato", "🥦 Broccoli", "🌰 Almonds", "🍦 Greek Yogurt", "🥩 Beef"};
        Spinner sp = findViewById(R.id.spinnerFood);
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items));
        
        findViewById(R.id.btnCalc).setOnClickListener(v -> {
            String[] keys = {"egg","chicken","rice","fish","paneer","oats","banana","milk","peanut","whey","potato","broccoli","almond","yogurt","beef"};
            Food f = db.get(keys[sp.getSelectedItemPosition()]);
            double w = Double.parseDouble(((EditText)findViewById(R.id.etWeight)).getText().toString().isEmpty() ? "100" : ((EditText)findViewById(R.id.etWeight)).getText().toString());
            int q = Integer.parseInt(((EditText)findViewById(R.id.etQty)).getText().toString().isEmpty() ? "1" : ((EditText)findViewById(R.id.etQty)).getText().toString());
            double r = w / 100;
            show(f.name, f.emoji, f.cal*r*q, f.pro*r*q, f.fat*r*q, f.carbs*r*q, f.tip);
        });
        
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            String q = ((EditText)findViewById(R.id.etSearch)).getText().toString().trim();
            if(q.isEmpty()) return;
            try {
                HttpURLConnection c = (HttpURLConnection)new URL("https://api.nal.usda.gov/fdc/v1/foods/search?api_key=DEMO_KEY&query="+q+"&pageSize=3").openConnection();
                Scanner s = new Scanner(c.getInputStream());
                StringBuilder b = new StringBuilder();
                while(s.hasNext()) b.append(s.nextLine());
                JSONArray foods = new JSONObject(b.toString()).getJSONArray("foods");
                String[] res = new String[foods.length()];
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
                    res[i] = f.getString("description")+"|"+cal+"|"+pro+"|"+fat+"|"+carbs;
                }
                ListView lv = findViewById(R.id.listSearchResults);
                lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, res));
                lv.setOnItemClickListener((p,vi,pos,id) -> {
                    String[] pts = res[pos].split("\\|");
                    selected = new Food(pts[0], "🌐", Double.parseDouble(pts[1]), Double.parseDouble(pts[2]), Double.parseDouble(pts[3]), Double.parseDouble(pts[4]), "Online data from USDA");
                    double w = Double.parseDouble(((EditText)findViewById(R.id.etWeight)).getText().toString().isEmpty() ? "100" : ((EditText)findViewById(R.id.etWeight)).getText().toString());
                    int qt = Integer.parseInt(((EditText)findViewById(R.id.etQty)).getText().toString().isEmpty() ? "1" : ((EditText)findViewById(R.id.etQty)).getText().toString());
                    double r = w / 100;
                    show(selected.name, selected.emoji, selected.cal*r*qt, selected.pro*r*qt, selected.fat*r*qt, selected.carbs*r*qt, selected.tip);
                });
            } catch(Exception e) {
                Toast.makeText(this, "Internet error! Use offline mode.", Toast.LENGTH_LONG).show();
            }
        });
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
}
