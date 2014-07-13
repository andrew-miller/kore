package com.pokemon.kore.ui;

import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Pair;

/**
 * A directed cyclic graph represented by a spanning tree with links to nodes in
 * the spanning tree (represented by paths over the spanning tree from the root)
 */
public interface LinkTree<E, V> {
  List<Pair<E, Either<LinkTree<E, V>, List<E>>>> edges();

  V vertex();
}