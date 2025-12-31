-- V3: Seed demo items for the Tempest WMS demo
-- This migration inserts 100 sample items across 5 categories for demonstration purposes.
-- Uses ON CONFLICT DO NOTHING to be idempotent.

-- Electronics (20 items)
INSERT INTO items (tenant_id, sku, name, description, active, created_by_user_id, updated_by_user_id)
VALUES
    ('demo-tenant', 'ELEC-001', 'Wireless Bluetooth Headphones', 'Over-ear noise-canceling headphones with 30hr battery', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-002', 'USB-C Charging Cable 6ft', 'Braided nylon USB-C to USB-C fast charging cable', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-003', 'Portable Power Bank 20000mAh', 'High capacity portable charger with dual USB ports', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-004', 'Wireless Mouse', 'Ergonomic wireless mouse with adjustable DPI', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-005', 'Mechanical Keyboard', 'RGB backlit mechanical keyboard with blue switches', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-006', 'Webcam 1080p', 'Full HD webcam with built-in microphone', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-007', 'USB Hub 7-Port', 'Powered USB 3.0 hub with individual switches', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-008', 'Laptop Stand Adjustable', 'Aluminum laptop stand with ventilation', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-009', 'Monitor Arm Single', 'Gas spring monitor arm for 13-32 inch displays', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-010', 'HDMI Cable 10ft', 'High-speed HDMI 2.1 cable 4K/120Hz support', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-011', 'Wireless Earbuds', 'True wireless earbuds with charging case', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-012', 'Smart Watch Band', 'Silicone replacement band for smartwatches', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-013', 'Phone Case Clear', 'Transparent protective phone case', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-014', 'Screen Protector 3-Pack', 'Tempered glass screen protector set', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-015', 'Wireless Charger Pad', 'Qi-compatible fast wireless charging pad', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-016', 'Bluetooth Speaker', 'Portable waterproof Bluetooth speaker', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-017', 'SD Card 128GB', 'High-speed SD card for cameras and devices', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-018', 'USB Flash Drive 64GB', 'Metal USB 3.0 flash drive', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-019', 'Cable Organizer Box', 'Desktop cable management box', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'ELEC-020', 'Surge Protector 6-Outlet', 'Power strip with surge protection', true, 'seed-migration', 'seed-migration')
ON CONFLICT (tenant_id, sku) DO NOTHING;

-- Office Supplies (20 items)
INSERT INTO items (tenant_id, sku, name, description, active, created_by_user_id, updated_by_user_id)
VALUES
    ('demo-tenant', 'OFFC-001', 'Ballpoint Pens 12-Pack', 'Medium point black ink pens', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-002', 'Sticky Notes 3x3 Assorted', 'Colorful sticky note pads 500 sheets', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-003', 'Notebook Spiral A5', 'College ruled spiral notebook 200 pages', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-004', 'Stapler Desktop', 'Heavy duty desktop stapler', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-005', 'Staples Standard 5000ct', 'Standard staples box of 5000', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-006', 'Paper Clips Jumbo 100ct', 'Large paper clips assorted colors', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-007', 'Binder Clips Medium 24ct', 'Medium binder clips black', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-008', 'Scissors 8 inch', 'Stainless steel office scissors', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-009', 'Tape Dispenser Desktop', 'Weighted tape dispenser with tape', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-010', 'Highlighters 6-Pack', 'Assorted color highlighter set', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-011', 'Markers Dry Erase 8-Pack', 'Chisel tip dry erase markers', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-012', 'Folder Manila 100ct', 'Letter size manila file folders', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-013', 'Binder 3-Ring 2 inch', 'White view binder with pockets', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-014', 'Sheet Protectors 100ct', 'Clear plastic sheet protectors', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-015', 'Desk Organizer', 'Mesh desk organizer with drawers', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-016', 'Pencil Cup Holder', 'Metal mesh pencil cup', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-017', 'Calculator Desktop', 'Large display desktop calculator', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-018', 'Correction Tape 6-Pack', 'White-out correction tape', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-019', 'Rubber Bands Assorted', 'Rubber bands various sizes 1lb', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'OFFC-020', 'Envelopes #10 500ct', 'White business envelopes', true, 'seed-migration', 'seed-migration')
ON CONFLICT (tenant_id, sku) DO NOTHING;

-- Apparel (20 items)
INSERT INTO items (tenant_id, sku, name, description, active, created_by_user_id, updated_by_user_id)
VALUES
    ('demo-tenant', 'APRL-001', 'T-Shirt Cotton Black M', 'Classic fit cotton t-shirt medium', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-002', 'T-Shirt Cotton Black L', 'Classic fit cotton t-shirt large', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-003', 'T-Shirt Cotton White M', 'Classic fit cotton t-shirt medium', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-004', 'T-Shirt Cotton White L', 'Classic fit cotton t-shirt large', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-005', 'Hoodie Zip-Up Gray M', 'Fleece zip-up hoodie medium', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-006', 'Hoodie Zip-Up Gray L', 'Fleece zip-up hoodie large', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-007', 'Joggers Athletic Black M', 'Athletic jogger pants medium', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-008', 'Joggers Athletic Black L', 'Athletic jogger pants large', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-009', 'Socks Athletic 6-Pack', 'Crew length athletic socks', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-010', 'Cap Baseball Black', 'Adjustable baseball cap', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-011', 'Beanie Knit Gray', 'Warm knit winter beanie', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-012', 'Gloves Winter Black', 'Touchscreen compatible winter gloves', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-013', 'Scarf Wool Navy', 'Soft wool winter scarf', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-014', 'Belt Leather Brown 34', 'Genuine leather dress belt', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-015', 'Belt Leather Black 34', 'Genuine leather dress belt', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-016', 'Polo Shirt Navy M', 'Classic polo shirt medium', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-017', 'Polo Shirt Navy L', 'Classic polo shirt large', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-018', 'Dress Shirt White M', 'Button-down dress shirt medium', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-019', 'Dress Shirt White L', 'Button-down dress shirt large', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'APRL-020', 'Jacket Windbreaker Black M', 'Lightweight windbreaker jacket', true, 'seed-migration', 'seed-migration')
ON CONFLICT (tenant_id, sku) DO NOTHING;

-- Home & Kitchen (20 items)
INSERT INTO items (tenant_id, sku, name, description, active, created_by_user_id, updated_by_user_id)
VALUES
    ('demo-tenant', 'HOME-001', 'Coffee Mug Ceramic 12oz', 'Classic ceramic coffee mug', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-002', 'Water Bottle Stainless 24oz', 'Insulated stainless steel water bottle', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-003', 'Lunch Container Set', 'Meal prep containers 10-pack', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-004', 'Cutting Board Bamboo', 'Large bamboo cutting board', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-005', 'Kitchen Towels 4-Pack', 'Cotton kitchen towels assorted', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-006', 'Dish Soap 28oz', 'Liquid dish soap lemon scent', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-007', 'Sponges 6-Pack', 'Heavy duty scrub sponges', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-008', 'Trash Bags 13gal 100ct', 'Tall kitchen trash bags', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-009', 'Paper Towels 6-Roll', 'Absorbent paper towel rolls', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-010', 'Food Storage Bags Gallon', 'Resealable food storage bags 75ct', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-011', 'Aluminum Foil 200sqft', 'Heavy duty aluminum foil roll', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-012', 'Plastic Wrap 400sqft', 'Cling wrap for food storage', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-013', 'Can Opener Manual', 'Stainless steel manual can opener', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-014', 'Measuring Cups Set', '4-piece measuring cup set', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-015', 'Measuring Spoons Set', '6-piece measuring spoon set', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-016', 'Mixing Bowls Set', 'Stainless steel mixing bowls 3-pack', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-017', 'Spatula Silicone', 'Heat resistant silicone spatula', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-018', 'Tongs Kitchen 12 inch', 'Stainless steel locking tongs', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-019', 'Timer Kitchen Digital', 'Magnetic digital kitchen timer', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'HOME-020', 'Oven Mitts Pair', 'Heat resistant silicone oven mitts', true, 'seed-migration', 'seed-migration')
ON CONFLICT (tenant_id, sku) DO NOTHING;

-- Sports & Outdoors (20 items)
INSERT INTO items (tenant_id, sku, name, description, active, created_by_user_id, updated_by_user_id)
VALUES
    ('demo-tenant', 'SPRT-001', 'Yoga Mat 6mm', 'Non-slip exercise yoga mat', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-002', 'Resistance Bands Set', '5-level resistance band set', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-003', 'Jump Rope Adjustable', 'Speed jump rope with bearings', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-004', 'Dumbbells 10lb Pair', 'Neoprene coated dumbbells', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-005', 'Exercise Ball 65cm', 'Anti-burst stability ball with pump', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-006', 'Foam Roller 18 inch', 'High density muscle foam roller', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-007', 'Gym Bag Duffel', 'Large gym duffel bag with shoe compartment', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-008', 'Towel Microfiber Sport', 'Quick-dry sports towel', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-009', 'Headband Athletic 3-Pack', 'Moisture-wicking athletic headbands', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-010', 'Wrist Wraps Pair', 'Weightlifting wrist support wraps', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-011', 'Knee Sleeve Compression', 'Neoprene knee compression sleeve', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-012', 'Water Bottle Sport 32oz', 'Squeeze sport water bottle', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-013', 'Backpack Hiking 40L', 'Lightweight hiking backpack', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-014', 'Flashlight LED Tactical', 'Rechargeable tactical flashlight', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-015', 'Compass Navigation', 'Orienteering compass with lanyard', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-016', 'First Aid Kit Compact', 'Portable first aid kit 100-piece', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-017', 'Sunscreen SPF50 6oz', 'Sport sunscreen water resistant', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-018', 'Insect Repellent 4oz', 'DEET-free insect repellent spray', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-019', 'Camping Hammock', 'Portable camping hammock with straps', true, 'seed-migration', 'seed-migration'),
    ('demo-tenant', 'SPRT-020', 'Cooler Bag Insulated', 'Soft-sided insulated cooler bag', true, 'seed-migration', 'seed-migration')
ON CONFLICT (tenant_id, sku) DO NOTHING;

