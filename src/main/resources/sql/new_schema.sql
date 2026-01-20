create type unit_type as enum ('PCS', 'KG', 'L');

create table dish_ingredient (
    id serial primary key ,
    id_dish int not null,
    id_ingredient int not null,
    quantity_required numeric(10,2) not null,
    unit unit_type not null,

    constraint fk_dish
        foreign key (id_dish) references dish(id) on delete cascade ,
    constraint fk_ingredient
        foreign key (id_ingredient) references ingredient(id) on delete cascade,
    constraint unique_dish_ingredient
        unique (id_dish, id_ingredient)

);

ALTER TABLE Dish
    RENAME COLUMN price TO selling_price;

ALTER TABLE Dish
    ADD COLUMN IF NOT EXISTS selling_price NUMERIC;

ALTER TABLE ingredient
    DROP COLUMN IF EXISTS id_dish;