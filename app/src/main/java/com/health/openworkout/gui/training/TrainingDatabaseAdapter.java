/*
 * Copyright (C) 2020 olie.xdev <olie.xdev@googlemail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.health.openworkout.gui.training;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.health.openworkout.R;
import com.health.openworkout.core.OpenWorkout;
import com.health.openworkout.core.datatypes.GitHubFile;
import com.health.openworkout.core.datatypes.TrainingPlan;
import com.health.openworkout.core.datatypes.WorkoutItem;
import com.health.openworkout.core.datatypes.WorkoutSession;
import com.health.openworkout.core.utils.PackageUtils;
import com.health.openworkout.gui.datatypes.GenericAdapter;
import com.health.openworkout.gui.workout.WorkoutsDatabaseAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import timber.log.Timber;

public class TrainingDatabaseAdapter extends RecyclerView.Adapter<TrainingDatabaseAdapter.ViewHolder> {
    private List<GitHubFile> gitHubFileList;
    private ViewHolder holder;
    private Context context;
    private static GenericAdapter.OnGenericClickListener onItemClickListener;

    public TrainingDatabaseAdapter(Context aContext, List<GitHubFile> gitHubFileList) {
        this.context = aContext;
        this.gitHubFileList = gitHubFileList;
    }

    public void setOnItemClickListener(GenericAdapter.OnGenericClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public TrainingDatabaseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trainingdatabase, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        this.holder = holder;
        GitHubFile gitHubFile = gitHubFileList.get(position);

        holder.nameView.setText(gitHubFile.getName().substring(0, gitHubFile.getName().length() - 4));
        DecimalFormat sizeFormat = new DecimalFormat("##0.00");
        double fileSize = (gitHubFile.getSize() / 1000000.0f);

        if (fileSize >= 1.0f) {
            holder.detailedView.setText(String.format(context.getString(R.string.label_package_size_mbytes), sizeFormat.format(fileSize)));
        } else {
            fileSize = fileSize * 1000.0f;
            holder.detailedView.setText(String.format(context.getString(R.string.label_package_size_kbytes), sizeFormat.format(fileSize)));
        }

        String displayName = gitHubFile.getName().substring(0, gitHubFile.getName().length() -4);
        File packageDir = new File(context.getFilesDir(),  displayName);

        if (packageDir.exists()) {
            List<TrainingPlan> trainingPlanList = OpenWorkout.getInstance().getTrainingPlans();

            for (TrainingPlan trainingPlan : trainingPlanList) {
                if (trainingPlan.getName().equals(displayName)) {
                    downloadCompleted(trainingPlan);
                }
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return gitHubFileList.indexOf(gitHubFileList.get(position));
    }

    @Override
    public int getItemCount() {
        return gitHubFileList.size();
    }

    public void updateProgressBar(long bytes, long totalBytes) {
        int percent = (int)(bytes / (double)totalBytes * 100.0);
        holder.progressBar.setProgress(percent);
    }

    public void downloadCompleted(TrainingPlan trainingPlan) {
        holder.progressBar.setProgress(100);
        if (trainingPlan.isImagePathExternal()) {
            holder.imgView.setImageURI(Uri.parse(trainingPlan.getImagePath()));
        } else {
            try {
                InputStream ims = context.getAssets().open("image/" + trainingPlan.getImagePath());
                holder.imgView.setImageDrawable(Drawable.createFromStream(ims, null));
                ims.close();
            } catch (IOException ex) {
                Timber.e(ex);
            }
        }
        holder.downloadView.setImageResource(R.drawable.ic_download_finished);
        holder.nameView.setEnabled(true);
        holder.detailedView.setEnabled(true);
        holder.itemView.setOnClickListener(null);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView;
        TextView nameView;
        TextView detailedView;
        ImageView downloadView;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgView = itemView.findViewById(R.id.imgView);
            nameView = itemView.findViewById(R.id.nameView);
            detailedView = itemView.findViewById(R.id.detailedView);
            downloadView = itemView.findViewById(R.id.downloadView);
            progressBar = itemView.findViewById(R.id.progressBar);

            progressBar.setMax(100);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(getAdapterPosition(), v);
                }
            });
        }
    }


}
