CREATE DATABASE benchs_satql;

\c benchs_satql

DROP TABLE IF EXISTS bench_diversification;
CREATE TABLE bench_diversification (
    id_exec FLOAT NOT NULL,
    id_model INTEGER NOT NULL,
    algo_utilise VARCHAR(40) NOT NULL,
    temps_exec FLOAT8 NOT NULL,
    UNIQUE (id_exec, id_model)
);
