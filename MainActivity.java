package in.sharmapharmacy.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final ArrayList<Medicine> medicines = new ArrayList<>();
    private final LinkedHashMap<String, CartLine> cart = new LinkedHashMap<>();
    private LinearLayout root, content;
    private EditText searchBox;
    private TextView summary;
    private android.content.SharedPreferences prefs;

    static class Medicine {
        String name, batch, expiry;
        double price;
        int stock;
        Medicine(String n, double p, int s, String b, String e) {
            name=n; price=p; stock=s; batch=b; expiry=e;
        }
    }
    static class CartLine {
        Medicine medicine;
        int qty = 1;
        int discount = 5;
        CartLine(Medicine m) { medicine=m; }
        double total() { return medicine.price * qty * (100-discount) / 100.0; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("sharma_pharmacy", MODE_PRIVATE);
        loadMedicines();
        if (medicines.isEmpty()) {
            medicines.add(new Medicine("Paracetamol 500 mg", 20, 40, "B001", "12/2027"));
            medicines.add(new Medicine("ORS Sachet", 22, 25, "B002", "08/2027"));
            medicines.add(new Medicine("Vitamin C 500 mg", 60, 18, "B003", "06/2027"));
            medicines.add(new Medicine("Cetirizine 10 mg", 15, 30, "B004", "11/2027"));
            saveMedicines();
        }
        showStore();
    }

    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String value, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(Color.rgb(35,45,55));
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setPadding(dp(4), dp(5), dp(4), dp(5));
        return t;
    }
    private Button button(String label, Runnable action) {
        Button b = new Button(this); b.setText(label);
        b.setOnClickListener(v -> action.run());
        return b;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(14), dp(8), dp(14), dp(8)); return l;
    }
    private void startScreen(String subtitle) {
        root = column(); root.setBackgroundColor(Color.rgb(246,249,247));
        TextView brand = text("SHARMA PHARMACY", 23, true);
        brand.setTextColor(Color.rgb(0,110,78)); brand.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(brand);
        root.addView(text(subtitle, 13, false));
        LinearLayout nav = new LinearLayout(this);
        nav.addView(button("Store", this::showStore), new LinearLayout.LayoutParams(0, dp(48), 1));
        nav.addView(button("Cart / Bill", this::showCart), new LinearLayout.LayoutParams(0, dp(48), 1));
        nav.addView(button("Admin", this::showAdmin), new LinearLayout.LayoutParams(0, dp(48), 1));
        root.addView(nav);
        ScrollView scroll = new ScrollView(this);
        content = column(); scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void showStore() {
        startScreen("Medicine search • Stock • Customer order estimate");
        searchBox = new EditText(this); searchBox.setSingleLine(true);
        searchBox.setHint("दवा का नाम खोजें…"); content.addView(searchBox);
        LinearLayout actions = new LinearLayout(this);
        actions.addView(button("Search", () -> renderMedicineList(searchBox.getText().toString())),
                new LinearLayout.LayoutParams(0, dp(48), 1));
        actions.addView(button("Refresh", () -> renderMedicineList(searchBox.getText().toString())),
                new LinearLayout.LayoutParams(0, dp(48), 1));
        content.addView(actions);
        summary = text("", 14, true); content.addView(summary);
        renderMedicineList("");
        searchBox.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            public void onTextChanged(CharSequence s,int st,int before,int count) { renderMedicineList(s.toString()); }
            public void afterTextChanged(android.text.Editable e) {}
        });
    }

    private void renderMedicineList(String query) {
        if (content == null) return;
        // Preserve search field and action rows while replacing product cards.
        int keep = 3;
        while (content.getChildCount() > keep) content.removeViewAt(keep);
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        int shown = 0;
        for (Medicine m : medicines) {
            if (!m.name.toLowerCase(Locale.ROOT).contains(q)) continue;
            shown++;
            LinearLayout card = column();
            card.setBackgroundColor(Color.WHITE);
            card.addView(text(m.name, 17, true));
            card.addView(text(String.format(Locale.getDefault(), "MRP/Price: ₹%.2f   •   Stock: %d", m.price, m.stock), 14, false));
            card.addView(text("Batch: " + m.batch + "   •   Expiry: " + m.expiry, 12, false));
            Button add = button(m.stock > 0 ? "Add to Cart" : "Out of stock", () -> addToCart(m));
            add.setEnabled(m.stock > 0);
            card.addView(add);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, dp(6), 0, dp(6)); content.addView(card, lp);
        }
        if (shown == 0) content.addView(text("कोई दवा नहीं मिली। Admin में नई दवा जोड़ें।", 15, false));
        if (summary != null) summary.setText("कुल दवाएँ: " + medicines.size() + "   •   Cart items: " + cart.size());
    }

    private void addToCart(Medicine m) {
        CartLine line = cart.get(m.name);
        if (line == null) cart.put(m.name, new CartLine(m));
        else if (line.qty < m.stock) line.qty++;
        else { toast("स्टॉक उपलब्ध नहीं है"); return; }
        toast(m.name + " cart में जुड़ गई");
        if (summary != null) summary.setText("कुल दवाएँ: " + medicines.size() + "   •   Cart items: " + cart.size());
    }

    private void showCart() {
        startScreen("Cart • Discount • Bill estimate");
        if (cart.isEmpty()) {
            content.addView(text("आपका cart खाली है। Store से दवाएँ जोड़ें।", 16, false));
            content.addView(button("Open Store", this::showStore)); return;
        }
        for (CartLine line : new ArrayList<>(cart.values())) {
            LinearLayout card = column(); card.setBackgroundColor(Color.WHITE);
            card.addView(text(line.medicine.name, 17, true));
            card.addView(text(String.format(Locale.getDefault(), "₹%.2f × %d", line.medicine.price, line.qty), 14, false));
            card.addView(text("Discount: " + line.discount + "%   |   Line total: ₹" + String.format(Locale.getDefault(), "%.2f", line.total()), 14, true));
            LinearLayout row = new LinearLayout(this);
            row.addView(button("−", () -> { if(line.qty > 1) line.qty--; else cart.remove(line.medicine.name); showCart(); }),
                    new LinearLayout.LayoutParams(0, dp(48), 1));
            row.addView(button("Qty +", () -> { if(line.qty < line.medicine.stock) line.qty++; else toast("स्टॉक उपलब्ध नहीं"); showCart(); }),
                    new LinearLayout.LayoutParams(0, dp(48), 1));
            row.addView(button("Discount", () -> discountDialog(line)),
                    new LinearLayout.LayoutParams(0, dp(48), 1));
            card.addView(row);
            content.addView(card);
        }
        double total = cart.values().stream().mapToDouble(CartLine::total).sum();
        content.addView(text(String.format(Locale.getDefault(), "Estimated total: ₹%.2f", total), 22, true));
        content.addView(text("यह अनुमानित बिल है। असली बिक्री/दवा वितरण से पहले फार्मासिस्ट जाँच करें।", 12, false));
        content.addView(button("Confirm Local Sale / Update Stock", this::confirmSale));
        content.addView(button("Clear Cart", () -> { cart.clear(); showCart(); }));
    }

    private void discountDialog(CartLine line) {
        EditText input = new EditText(this); input.setInputType(2); input.setText(String.valueOf(line.discount));
        input.setHint("5 से 80 के बीच प्रतिशत");
        new AlertDialog.Builder(this).setTitle("Discount (%)").setMessage("छूट 5% से 80% के बीच रखें।")
                .setView(input).setNegativeButton("Cancel", null).setPositiveButton("Save", (d,w) -> {
                    try {
                        int val = Integer.parseInt(input.getText().toString().trim());
                        if (val < 5 || val > 80) { toast("Discount 5% से 80% के बीच होना चाहिए"); return; }
                        line.discount = val; showCart();
                    } catch(Exception e) { toast("सही discount लिखें"); }
                }).show();
    }

    private void confirmSale() {
        new AlertDialog.Builder(this).setTitle("बिक्री की पुष्टि")
                .setMessage("यह demo स्थानीय फोन में स्टॉक घटाएगा। क्या जारी रखें?")
                .setNegativeButton("Cancel", null).setPositiveButton("Confirm", (d,w) -> {
                    for (CartLine line : cart.values()) {
                        if (line.qty > line.medicine.stock) { toast("स्टॉक बदल गया है; बिक्री नहीं हुई"); return; }
                    }
                    for (CartLine line : cart.values()) line.medicine.stock -= line.qty;
                    saveMedicines(); cart.clear(); toast("Demo sale सेव हुई और stock अपडेट हुआ"); showStore();
                }).show();
    }

    private void showAdmin() {
        startScreen("Admin • Local inventory management");
        content.addView(text("दवा का स्टॉक और विवरण इस फोन पर सेव होता है। यह अभी login-protected या cloud-connected नहीं है।", 13, false));
        content.addView(button("Add New Medicine", this::addMedicineDialog));
        content.addView(button("Export inventory text", this::exportInventory));
        for (Medicine m : medicines) {
            LinearLayout card = column(); card.setBackgroundColor(Color.WHITE);
            card.addView(text(m.name, 16, true));
            card.addView(text(String.format(Locale.getDefault(), "₹%.2f • Stock %d • Batch %s • Expiry %s",m.price,m.stock,m.batch,m.expiry),13,false));
            LinearLayout row = new LinearLayout(this);
            row.addView(button("Edit stock", () -> stockDialog(m)), new LinearLayout.LayoutParams(0, dp(48), 1));
            row.addView(button("Edit price", () -> priceDialog(m)), new LinearLayout.LayoutParams(0, dp(48), 1));
            card.addView(row); content.addView(card);
        }
    }

    private void addMedicineDialog() {
        LinearLayout form = column();
        EditText name = field("Medicine name");
        EditText price = field("Price ₹"); price.setInputType(8194);
        EditText stock = field("Opening stock"); stock.setInputType(2);
        EditText batch = field("Batch number");
        EditText expiry = field("Expiry (MM/YYYY)");
        form.addView(name); form.addView(price); form.addView(stock); form.addView(batch); form.addView(expiry);
        new AlertDialog.Builder(this).setTitle("Add medicine").setView(form)
                .setNegativeButton("Cancel", null).setPositiveButton("Save", (d,w) -> {
                    try {
                        String n=name.getText().toString().trim();
                        double p=Double.parseDouble(price.getText().toString().trim());
                        int s=Integer.parseInt(stock.getText().toString().trim());
                        if(n.isEmpty() || p <= 0 || s < 0) throw new Exception();
                        for(Medicine m:medicines) if(m.name.equalsIgnoreCase(n)) { toast("यह दवा पहले से है"); return; }
                        medicines.add(new Medicine(n,p,s,batch.getText().toString().trim(),expiry.getText().toString().trim()));
                        saveMedicines(); showAdmin(); toast("Medicine added");
                    } catch(Exception e) { toast("नाम, सही कीमत और stock भरें"); }
                }).show();
    }
    private EditText field(String hint) { EditText e=new EditText(this); e.setHint(hint); e.setSingleLine(true); return e; }
    private void stockDialog(Medicine m) {
        EditText input=field("New stock"); input.setInputType(2); input.setText(String.valueOf(m.stock));
        new AlertDialog.Builder(this).setTitle("Update stock: "+m.name).setView(input)
                .setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
                    try { int s=Integer.parseInt(input.getText().toString()); if(s<0) throw new Exception();
                        m.stock=s; saveMedicines(); showAdmin(); } catch(Exception e){toast("सही stock लिखें");}
                }).show();
    }
    private void priceDialog(Medicine m) {
        EditText input=field("New price ₹"); input.setInputType(8194); input.setText(String.valueOf(m.price));
        new AlertDialog.Builder(this).setTitle("Update price").setView(input)
                .setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
                    try { double p=Double.parseDouble(input.getText().toString()); if(p<=0) throw new Exception();
                        m.price=p; saveMedicines(); showAdmin(); } catch(Exception e){toast("सही कीमत लिखें");}
                }).show();
    }
    private void exportInventory() {
        StringBuilder b=new StringBuilder("SHARMA PHARMACY INVENTORY\n");
        for(Medicine m:medicines) b.append(m.name).append(" | ₹").append(m.price).append(" | Stock ").append(m.stock)
                .append(" | Batch ").append(m.batch).append(" | Expiry ").append(m.expiry).append("\n");
        new AlertDialog.Builder(this).setTitle("Inventory export preview").setMessage(b.toString())
                .setPositiveButton("Copy", (d,w)->{
                    android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("Inventory",b.toString())); toast("Inventory text copied");
                }).setNegativeButton("Close",null).show();
    }

    private void saveMedicines() {
        JSONArray arr=new JSONArray();
        try {
            for(Medicine m:medicines) {
                JSONObject o=new JSONObject(); o.put("name",m.name); o.put("price",m.price); o.put("stock",m.stock);
                o.put("batch",m.batch); o.put("expiry",m.expiry); arr.put(o);
            }
            prefs.edit().putString("medicines",arr.toString()).apply();
        } catch(Exception ignored) {}
    }
    private void loadMedicines() {
        String raw=prefs.getString("medicines","");
        if(raw.isEmpty()) return;
        try {
            JSONArray arr=new JSONArray(raw);
            for(int i=0;i<arr.length();i++) {
                JSONObject o=arr.getJSONObject(i);
                medicines.add(new Medicine(o.optString("name"),o.optDouble("price"),o.optInt("stock"),
                        o.optString("batch"),o.optString("expiry")));
            }
        } catch(Exception ignored) { medicines.clear(); }
    }
    private void toast(String s) { Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
}
