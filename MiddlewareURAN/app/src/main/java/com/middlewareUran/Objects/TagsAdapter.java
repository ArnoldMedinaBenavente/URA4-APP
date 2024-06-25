package com.middlewareUran.Objects;

import android.content.Context;
import android.nfc.Tag;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.middlewareUran.R;

import java.util.ArrayList;

public class TagsAdapter extends BaseAdapter {
    Context context;
    ArrayList<Tags> arrayList;


    public TagsAdapter(Context context, ArrayList<Tags> arrayList) {
        this.context = context;
        this.arrayList = arrayList;


    }


    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.item_tag_layout,null);
        TextView txt_estacion = view.findViewById(R.id.textView_estacion);
        TextView txt_chofer = view.findViewById(R.id.textView_chofer);
        TextView txt_placa = view.findViewById(R.id.textView_placa);
        TextView txt_count = view.findViewById(R.id.textView_count);
        TextView txt_epc = view.findViewById(R.id.textView_epc);

            Tags tag  = arrayList.get(i);
            String estacion = "";
        switch (tag.antena) {
            case "1":
                estacion = "Lavado";
                break;
            case "2":
                estacion = "Checa mezclas";
                break;
            case "3":
                estacion = "Carga";
                break;
            case "4":
                estacion = "Salida";
                break;
            default:
                estacion = tag.antena;
                break;
        }
        int count = Integer.parseInt(tag.conut) + 1;
        tag.setConut(String.valueOf(count));
         txt_estacion.setText(estacion);
         txt_chofer.setText(tag.chofer);
         txt_placa .setText(tag.placas);
         txt_count.setText(String.valueOf(count));
         txt_epc .setText(tag.epc);
        return view;
    }
}
