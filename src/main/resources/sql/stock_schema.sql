create type mouvement_type as enum('IN', 'OUT');

create table stock_movement (
    id serial primary key,
    id_ingredient int not null,
    quantity numeric (10,2) not null,
    unit unit_type not null ,
    type mouvement_type not null,
    creation_datetime timestamp default current_timestamp not null,
    constraint fk_ingredient
        foreign key (id_ingredient)
        references ingredient(id)
        on delete cascade
)