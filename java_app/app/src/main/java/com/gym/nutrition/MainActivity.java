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
        String name; double cal, pro, fat, carbs;
        Food(String n, double c, double p, double f, double cb) {
            name = n; cal = c; pro = p; fat = f; carbs = cb;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        
        db.put("egg", new Food("Boiled Egg", 128, 12.3, 8.8, 0.2));
        db.put("chicken", new Food("Chicken Breast", 165, 31, 3.6, 0));
        db.put("rice", new Food("White Rice", 130, 2.7, 0.3, 28));
        db.put("fish", new Food("Salmon", 208, 20, 13, 0));
        db.put("paneer", new Food("Paneer", 265, 18, 21, 1.2));
        db.put("oats", new Food("Oats", 389, 16.9, 6.9, 66));
        
        String[] items = {"🥚 Egg", "🍗 Chicken", "🍚 Rice", "🐟 Fish", "🧀 Paneer", "🌾 Oats"};
        Spinner sp = findViewById(R.id.spinnerFood);
        sp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items));
        
        findViewById(R.id.btnCalc).setOnClickListener(v -> {
            String[] keys = {"egg","chicken","rice","fish","paneer","oats"};
            Food f = db.get(keys[sp.getSelectedItemPosition()]);
            double w = Double.parseDouble(((EditText)findViewById(R.id.etWeight)).getText().toString().isEmpty() ? "100" : ((EditText)findViewById(R.id.etWeight)).getText().toString());
            int q = Integer.parseInt(((EditText)findViewById(R.id.etQty)).getText().toString().isEmpty() ? "1" : ((EditText)findViewById(R.id.etQty)).getText().toString());
            double r = w / 100;
            show(f.name, f.cal*r*q, f.pro*r*q, f.fat*r*q, f.carbs*r*q);
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
                    selected = new Food(pts[0], Double.parseDouble(pts[1]), Double.parseDouble(pts[2]), Double.parseDouble(pts[3]), Double.parseDouble(pts[4]));
                    double w = Double.parseDouble(((EditText)findViewById(R.id.etWeight)).getText().toString().isEmpty() ? "100" : ((EditText)findViewById(R.id.etWeight)).getText().toString());
                    int qt = Integer.parseInt(((EditText)findViewById(R.id.etQty)).getText().toString().isEmpty() ? "1" : ((EditText)findViewById(R.id.etQty)).getText().toString());
                    double r = w / 100;
                    show(selected.name, selected.cal*r*qt, selected.pro*r*qt, selected.fat*r*qt, selected.carbs*r*qt);
                });
            } catch(Exception e) {
                Toast.makeText(this, "❌ Internet error!", Toast.LENGTH_LONG).show();
            }
        });
    }
    
    void show(String n, double c, double p, double f, double cb) {
        findViewById(R.id.layoutResults).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.tvResult)).setText(String.format("🔥 %.1f kcal\n💪 %.1f g\n🥑 %.1f g\n🍞 %.1f g", c, p, f, cb));
        ((ProgressBar)findViewById(R.id.proteinBar)).setProgress(Math.min((int)((p/150)*100),100));
        ((ProgressBar)findViewById(R.id.calBar)).setProgress(Math.min((int)((c/2500)*100),100));
        ((TextView)findViewById(R.id.tvProteinPct)).setText("Protein: "+Math.min((int)((p/150)*100),100)+"%");
        ((TextView)findViewById(R.id.tvCalPct)).setText("Calories: "+Math.min((int)((c/2500)*100),100)+"%");
    }
}
