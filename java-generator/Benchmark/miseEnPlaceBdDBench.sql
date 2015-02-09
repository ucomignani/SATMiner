CREATE DATABASE benchs_satql;

\c benchs_satql

DROP TABLE IF EXISTS diversification.bench_modeles;
DROP TABLE IF EXISTS diversification.bench_temps;
DROP SCHEMA IF EXISTS benchs_satql.diversification;

CREATE SCHEMA diversification;

CREATE TABLE diversification.bench_temps (
    id_exec FLOAT PRIMARY KEY,
    algo_utilise VARCHAR(40) NOT NULL,
    temps_exec FLOAT8 NOT NULL
);

CREATE TABLE diversification.bench_modeles (
    id_exec FLOAT ,
    id_model INTEGER NOT NULL,
    resultat_requete VARCHAR(1000) NOT NULL,
    distances FLOAT NOT NULL,    
    UNIQUE(id_exec,id_model),
    FOREIGN KEY(id_exec) REFERENCES diversification.bench_temps(id_exec)
);
