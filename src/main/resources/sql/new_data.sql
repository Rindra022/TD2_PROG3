insert into dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
values (1, 1, 0.20, 'KG'),
       (1, 2, 0.15, 'KG'),
       (2, 3, 1.00, 'KG'),
       (4, 4, 0.30, 'KG'),
       (4, 5, 0.20, 'KG');


select setval('dish_ingredient_id_seq', (select max(id) from dish_ingredient));

update dish
set selling_price = 3500.00
where id = 1;
update dish
set selling_price = 12000.00
where id = 2;
update dish
set selling_price = 8000.00
where id = 4;