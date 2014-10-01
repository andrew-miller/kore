package com.pokemon.kore.ui;

import com.pokemon.kore.codes.З2Bytes;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Pair;

/**
 * A directed cyclic graph represented by a spanning tree with links to nodes in
 * the spanning tree (represented by paths over the spanning tree from the root)
 * and links to other LinkTree2s by hash/path pairs
 */
public interface LinkTree2<E, V> {
  List<Pair<E, Either3<LinkTree2<E, V>, List<E>, Pair<З2Bytes, List<E>>>>>
      edges();

  V vertex();
}