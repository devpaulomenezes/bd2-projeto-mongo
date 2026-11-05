package br.edu.ifpb.es.bd2.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ClienteSnapshotEmbedded {

    private String nome;

    private String email;
}
