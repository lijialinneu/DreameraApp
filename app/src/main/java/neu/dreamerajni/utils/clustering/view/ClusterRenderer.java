/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */

package neu.dreamerajni.utils.clustering.view;


import java.util.Set;

import neu.dreamerajni.utils.clustering.Cluster;
import neu.dreamerajni.utils.clustering.ClusterItem;
import neu.dreamerajni.utils.clustering.ClusterManager;

//import mapapi.clusterutil.clustering.Cluster;
//import mapapi.clusterutil.clustering.ClusterItem;
//import mapapi.clusterutil.clustering.ClusterManager;

/**
 * Renders clusters.
 */
public interface ClusterRenderer<T extends ClusterItem> {

    /**
     * Called when the view needs to be updated because new clusters need to be displayed.
     * @param clusters the clusters to be displayed.
     */
    void onClustersChanged(Set<? extends Cluster<T>> clusters);

    void setOnClusterClickListener(ClusterManager.OnClusterClickListener<T> listener);

    void setOnClusterInfoWindowClickListener(ClusterManager.OnClusterInfoWindowClickListener<T> listener);

    void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<T> listener);

    void setOnClusterItemInfoWindowClickListener(ClusterManager.OnClusterItemInfoWindowClickListener<T> listener);

    /**
     * Called when the view is added.
     */
    void onAdd();

    /**
     * Called when the view is removed.
     */
    void onRemove();
}