package com.example.polivinc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class MemberProgressAdapter extends BaseAdapter {

    private Context context;
    private List<MemberProgress> members;

    public MemberProgressAdapter(Context context, List<MemberProgress> members) {
        this.context = context;
        this.members = members;
    }

    @Override
    public int getCount() {
        return members.size();
    }

    @Override
    public Object getItem(int position) {
        return members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.member_progress_item, parent, false);
        }

        // Obtener las referencias a los elementos del diseño
        TextView memberName = convertView.findViewById(R.id.Name);
        TextView memberProgressText = convertView.findViewById(R.id.memberProgressText);
        ProgressBar memberProgressBar = convertView.findViewById(R.id.memberProgressBar);

        // Obtener los datos del miembro actual
        MemberProgress member = members.get(position);

        // Configurar el nombre del miembro en lugar del ID
        memberName.setText(member.getMemberName()); // Cambiado para mostrar el nombre
        String progressText = member.getCompletedTasks() + "/" + member.getTotalTasks();
        memberProgressText.setText(progressText);

        // Configurar la barra de progreso
        memberProgressBar.setProgress(member.getProgressPercentage());

        return convertView;
    }
}
