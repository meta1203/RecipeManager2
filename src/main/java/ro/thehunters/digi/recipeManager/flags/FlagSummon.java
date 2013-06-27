package ro.thehunters.digi.recipeManager.flags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import ro.thehunters.digi.recipeManager.ErrorReporter;
import ro.thehunters.digi.recipeManager.Files;
import ro.thehunters.digi.recipeManager.Messages;
import ro.thehunters.digi.recipeManager.RecipeManager;
import ro.thehunters.digi.recipeManager.Tools;

public class FlagSummon extends Flag
{
    // Flag definition and documentation
    
    private static final FlagType TYPE;
    protected static final String[] A;
    protected static final String[] D;
    protected static final String[] E;
    
    static
    {
        TYPE = FlagType.SUMMON;
        
        A = new String[]
        {
            "{flag} <type> | [arguments]",
        };
        
        String argFormat = "  %-26s = %s";
        
        D = new String[]
        {
            "Summons a creature.",
            "Using this flag more than once will add more creatures.",
            "",
            "The <type> argument can be a living entity type, you can find all entity types in '" + Files.FILE_INFO_NAMES + "' file.",
            "",
            "Optionally you can add some arguments separated by | character, those being:",
            String.format(argFormat, "noeffect", "no spawning particle effects on creature."),
            String.format(argFormat, "noremove", "prevents creature from being removed if nobody is near it."),
            String.format(argFormat, "mountnext", "this creature will mount the next creature definition that triggers after it."),
            String.format(argFormat, "chance <0.01-100>%", "chance of the creature to spawn, this value is for individual creatures."),
            String.format(argFormat, "num <number>", "spawn more cloned creatures."),
            String.format(argFormat, "spread <range>", "spawns creature(s) spread within block range instead of on top of workbench or furnace. (WARNING: can be CPU intensive)"),
            String.format(argFormat, "target", "creature targets crafter, that means monsters attack and animals follow and the rest do nothing"),
            String.format(argFormat, "hit", "crafter will fake-attack the creature to provoke it into attacking or scare it away."),
            String.format(argFormat, "onfire <time>", "spawn creature on fire for <time> amount of seconds, value can be float."),
            String.format(argFormat, "pickup [true/false]", "change if creature can pick-up dropped items."),
            String.format(argFormat, "pet [nosit]", "makes creature owned by crafter, only works for wolf and ocelot, optionally specify 'nosit' to not spawn creature in sit stance."),
            String.format(argFormat, "angry", "makes creature angry, only works for wolves and pigzombies; you can't use 'pet' with this."),
            String.format(argFormat, "cat <type>", "ocelot type, available values: " + Tools.collectionToString(Arrays.asList(Ocelot.Type.values())).toLowerCase()),
            String.format(argFormat, "saddle [mount]", "adds saddle on creature, only works for pig, optionally you can specify 'mount' to make crafter mount creature."),
            String.format(argFormat, "color <dye>", "sets the color of animal, only works for sheep and pet wolf; values can be found in '" + Files.FILE_INFO_NAMES + "' file at 'DYE COLORS' section."),
            String.format(argFormat, "shearedsheep", "sets the sheep as sheared, only works for sheep."),
            String.format(argFormat, "villager <type>", "set the villager profession, values: " + Tools.collectionToString(Arrays.asList(Villager.Profession.values())).toLowerCase()),
            String.format(argFormat, "skeleton <type>", "set the skeleton type, values: " + Tools.collectionToString(Arrays.asList(SkeletonType.values())).toLowerCase()),
            String.format(argFormat, "zombievillager", "makes zombie a zombie villager, only works on zombies."),
            String.format(argFormat, "poweredcreeper", "makes creeper a powered one, only works for creepers."),
            String.format(argFormat, "playerirongolem", "marks iron golem as player-made."),
            String.format(argFormat, "name <text>", "sets the creature's name, supports colors (<red>, &3, etc)."),
            String.format(argFormat, "nohidename", "don't hide name plate when not aiming at creature."),
            String.format(argFormat, "hp <health> [max]", "set creature's health and optionally max health"),
            String.format(argFormat, "baby", "spawn creature as a baby, works with animals, villagers and zombies."),
            String.format(argFormat, "agelock", "prevent the creature from maturing or getting ready for mating, works with animals and villagers."),
            String.format(argFormat, "nobreed", "prevent the creature being able to breed, works for animals and villagers."),
            String.format(argFormat, "head <item> [drop%]", "equip an item on the creature's head with optional drop chance."),
            String.format(argFormat, "chest <item> [drop%]", "equip an item on the creature's chest with optional drop chance."),
            String.format(argFormat, "legs <item> [drop%]", "equip an item on the creature's legs with optional drop chance."),
            String.format(argFormat, "feet <item> [drop%]", "equip an item on the creature's feet with optional drop chance."),
            String.format(argFormat, "hand <item> [drop%]", "equip an item on the creature's hand with optional drop chance; for enderman it only uses material and data from the item."),
            String.format(argFormat, "potion <type> [time] [amp]", "adds potion effect on the spawned creature; for <type> see '" + Files.FILE_INFO_NAMES + "' at 'POTION EFFECT TYPE'; [time] can be a decimal of duration in seconds; [amp] can be an integer that defines amplifier; this argument can be used more than once to add more effects."),
            "",
            "These arguments can be used in any order and they're all optional.",
        };
        
        E = new String[]
        {
            "{flag} cow",
            "{flag} skeleton | hand bow // skeletons spawn without weapons, you need to give it one",
            "{flag} zombie | zombievillager | baby | chest chainmail_chestplate 25% | legs chainmail_leggings 25% | hand iron_sword 50% // baby villager zombie warrior",
            "{flag} sheep | color pink | name <light_purple>Pony",
            "{flag} ocelot | cat redcat | pet | potion speed 30 5",
            "// chicken on a villager and villager on a cow:",
            "{flag} chicken | mountnext",
            "{flag} villager | mountnext",
            "{flag} cow",
        };
    }
    
    // Flag code
    
    public class Customization implements Cloneable
    {
        private EntityType type = EntityType.PIG;
        private boolean noEffect = false;
        private boolean noRemove = false;
        private boolean mountNext = false;
        private float chance = 100.0f;
        private int num = 1;
        private int spread = 0;
        private float onFire = 0;
        private Boolean pickup = null;
        private boolean target = false;
        private boolean hit = false;
        private boolean angry = false;
        private boolean pet = false;
        private boolean noSit = false;
        private Ocelot.Type cat = null;
        private boolean saddle = false;
        private boolean mount = false;
        private DyeColor color = null;
        private boolean shearedSheep = false;
        private SkeletonType skeleton = null;
        private boolean zombieVillager = false;
        private Villager.Profession villager = null;
        private boolean poweredCreeper = false;
        private boolean playerIronGolem = false;
        private int pigAnger = 0;
        private String name = null;
        private boolean noHideName = false;
        private int hp = 0;
        private int maxHp = 0;
        private boolean baby = false;
        private boolean ageLock = false;
        private boolean noBreed = false;
        private ItemStack[] equip = new ItemStack[5];
        private float[] drop = new float[5];
        private List<PotionEffect> potions = new ArrayList<PotionEffect>();
        
        public Customization(EntityType type)
        {
            this.type = type;
        }
        
        public Customization(Customization c)
        {
            type = c.type;
            noEffect = c.noEffect;
            noRemove = c.noRemove;
            chance = c.chance;
            num = c.num;
            spread = c.spread;
            onFire = c.onFire;
            pickup = c.pickup;
            target = c.target;
            hit = c.hit;
            angry = c.angry;
            pet = c.pet;
            noSit = c.noSit;
            cat = c.cat;
            saddle = c.saddle;
            mount = c.mount;
            color = c.color;
            shearedSheep = c.shearedSheep;
            skeleton = c.skeleton;
            zombieVillager = c.zombieVillager;
            villager = c.villager;
            poweredCreeper = c.poweredCreeper;
            playerIronGolem = c.playerIronGolem;
            name = c.name;
            noHideName = c.noHideName;
            hp = c.hp;
            maxHp = c.maxHp;
            baby = c.baby;
            ageLock = c.ageLock;
            noBreed = c.noBreed;
            System.arraycopy(c.equip, 0, equip, 0, c.equip.length);
            System.arraycopy(c.drop, 0, drop, 0, c.drop.length);
            potions.addAll(c.potions);
            mountNext = c.mountNext;
        }
        
        @Override
        public Customization clone()
        {
            return new Customization(this);
        }
        
        public List<LivingEntity> spawn(Location location, Player player)
        {
            List<LivingEntity> entities = new ArrayList<LivingEntity>(this.num);
            World world = location.getWorld();
            
            for(int num = 0; num < this.num; num++)
            {
                if(spread > 0)
                {
                    int minX = location.getBlockX() - spread / 2;
                    int minY = location.getBlockY() - spread / 2;
                    int minZ = location.getBlockZ() - spread / 2;
                    int maxX = location.getBlockX() + spread / 2;
                    int maxY = location.getBlockY() + spread / 2;
                    int maxZ = location.getBlockZ() + spread / 2;
                    
                    int tries = spread * 10;
                    boolean found = false;
                    
                    while(tries-- > 0)
                    {
                        int x = minX + RecipeManager.random.nextInt(maxX - minX);
                        int z = minZ + RecipeManager.random.nextInt(maxZ - minZ);
                        int y = 0;
                        
                        for(y = maxY; y >= minY; y--)
                        {
                            if(!Material.getMaterial(world.getBlockTypeIdAt(x, y, z)).isSolid())
                            {
                                found = true;
                                break;
                            }
                        }
                        
                        if(found)
                        {
                            location.setX(x);
                            location.setY(y);
                            location.setZ(z);
                            break;
                        }
                    }
                    
                    if(!found)
                    {
                        Messages.debug("Couldn't find suitable location after " + (spread * 10) + " tries, using center.");
                    }
                    
                    location.add(0.5, 0, 0.5);
                }
                
                LivingEntity ent = (LivingEntity)world.spawnEntity(location, type);
                entities.add(ent);
                
                if(!noEffect)
                {
                    world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 20);
                }
                
                if(name != null)
                {
                    ent.setCustomName(name);
                    ent.setCustomNameVisible(noHideName);
                }
                
                if(onFire > 0.0f)
                {
                    ent.setFireTicks((int)Math.ceil(onFire * 20.0));
                }
                
                if(pickup != null)
                {
                    ent.setCanPickupItems(pickup);
                }
                
                if(pet && ent instanceof Tameable)
                {
                    Tameable npc = (Tameable)ent;
                    npc.setOwner(player);
                    npc.setTamed(true);
                }
                
                if(ent instanceof Wolf)
                {
                    Wolf npc = (Wolf)ent;
                    
                    if(pet)
                    {
                        if(noSit)
                        {
                            npc.setSitting(false);
                        }
                        
                        if(color != null)
                        {
                            npc.setCollarColor(color);
                        }
                    }
                    else if(angry)
                    {
                        npc.setAngry(true);
                    }
                }
                
                if(ent instanceof Ocelot)
                {
                    Ocelot npc = (Ocelot)ent;
                    
                    if(pet && noSit)
                    {
                        npc.setSitting(false);
                    }
                    
                    if(cat != null)
                    {
                        npc.setCatType(cat);
                    }
                }
                
                if(hp > 0)
                {
                    ent.setHealth(hp);
                    
                    if(maxHp > 0)
                    {
                        ent.setMaxHealth(maxHp);
                    }
                }
                
                if(ent instanceof Ageable)
                {
                    Ageable npc = (Ageable)ent;
                    
                    if(baby)
                    {
                        npc.setBaby();
                    }
                    
                    if(ageLock)
                    {
                        npc.setAgeLock(true);
                    }
                    
                    if(noBreed)
                    {
                        npc.setBreed(false);
                    }
                }
                
                if(saddle && ent instanceof Pig)
                {
                    Pig npc = (Pig)ent;
                    npc.setSaddle(true);
                    
                    if(mount)
                    {
                        npc.setPassenger(player);
                    }
                }
                
                if(ent instanceof Zombie)
                {
                    Zombie npc = (Zombie)ent;
                    
                    if(baby)
                    {
                        npc.setBaby(true);
                    }
                    
                    if(zombieVillager)
                    {
                        npc.setVillager(true);
                    }
                }
                
                if(villager != null && ent instanceof Villager)
                {
                    Villager npc = (Villager)ent;
                    npc.setProfession(villager);
                }
                
                if(poweredCreeper && ent instanceof Creeper)
                {
                    Creeper npc = (Creeper)ent;
                    npc.setPowered(true);
                }
                
                if(playerIronGolem && ent instanceof IronGolem)
                {
                    IronGolem npc = (IronGolem)ent;
                    npc.setPlayerCreated(true); // TODO what exacly does this do ?
                }
                
                if(shearedSheep && ent instanceof Sheep)
                {
                    Sheep npc = (Sheep)ent;
                    npc.setSheared(true);
                }
                
                if(color != null && ent instanceof Colorable)
                {
                    Colorable npc = (Colorable)ent;
                    npc.setColor(color);
                }
                
                if(skeleton != null && ent instanceof Skeleton)
                {
                    Skeleton npc = (Skeleton)ent;
                    npc.setSkeletonType(skeleton);
                }
                
                if(target && ent instanceof Creature)
                {
                    Creature npc = (Creature)ent;
                    npc.setTarget(player);
                }
                
                if(pigAnger > 0 && ent instanceof PigZombie)
                {
                    PigZombie npc = (PigZombie)ent;
                    npc.setAnger(pigAnger);
                }
                
                if(hit)
                {
                    ent.damage(0, player);
                    ent.setVelocity(new Vector());
                }
                
                if(!potions.isEmpty())
                {
                    for(PotionEffect effect : potions)
                    {
                        ent.addPotionEffect(effect, true);
                    }
                }
                
                ent.setRemoveWhenFarAway(!noRemove);
                
                EntityEquipment eq = ent.getEquipment();
                
                for(int i = 0; i < equip.length; i++)
                {
                    ItemStack item = equip[i];
                    
                    if(item == null)
                    {
                        continue;
                    }
                    
                    switch(i)
                    {
                        case 0:
                            eq.setHelmet(item);
                            eq.setHelmetDropChance(drop[i]);
                            break;
                        
                        case 1:
                            eq.setChestplate(item);
                            eq.setChestplateDropChance(drop[i]);
                            break;
                        
                        case 2:
                            eq.setLeggings(item);
                            eq.setLeggingsDropChance(drop[i]);
                            break;
                        
                        case 3:
                            eq.setBoots(item);
                            eq.setBootsDropChance(drop[i]);
                            break;
                        
                        case 4:
                        {
                            if(ent instanceof Enderman)
                            {
                                Enderman npc = (Enderman)ent;
                                npc.setCarriedMaterial(item.getData());
                            }
                            else
                            {
                                eq.setItemInHand(item);
                                eq.setItemInHandDropChance(drop[i]);
                            }
                            
                            break;
                        }
                    }
                }
            }
            
            return entities;
        }
        
        public EntityType getType()
        {
            return type;
        }
        
        public void setType(EntityType type)
        {
            this.type = type;
        }
        
        public boolean isNoEffect()
        {
            return noEffect;
        }
        
        public void setNoEffect(boolean noEffect)
        {
            this.noEffect = noEffect;
        }
        
        public boolean isNoRemove()
        {
            return noRemove;
        }
        
        public void setNoRemove(boolean noRemove)
        {
            this.noRemove = noRemove;
        }
        
        public float getChance()
        {
            return chance;
        }
        
        public void setChance(float chance)
        {
            if(chance < 0.01f || chance > 100.0f)
            {
                this.chance = Math.min(Math.max(chance, 0.01f), 100.0f);
                
                ErrorReporter.warning("Flag " + getType() + " has chance value less than 0.01 or higher than 100.0, value trimmed.");
            }
            else
            {
                this.chance = chance;
            }
        }
        
        public int getNum()
        {
            return num;
        }
        
        public void setNum(int num)
        {
            if(num < 1)
            {
                this.num = 1;
                
                ErrorReporter.warning("The " + getType() + " flag can't have 'num' argument less than 1, set to 1.");
            }
            else
            {
                this.num = num;
            }
        }
        
        public int getSpread()
        {
            return spread;
        }
        
        public void setSpread(int spread)
        {
            this.spread = spread;
        }
        
        public boolean isTarget()
        {
            return target;
        }
        
        public void setTarget(boolean target)
        {
            this.target = target;
        }
        
        public boolean isPet()
        {
            return pet;
        }
        
        public void setPet(boolean pet)
        {
            this.pet = pet;
        }
        
        public boolean isSaddle()
        {
            return saddle;
        }
        
        public void setSaddle(boolean saddle)
        {
            this.saddle = saddle;
        }
        
        public boolean isMount()
        {
            return mount;
        }
        
        public void setMount(boolean mount)
        {
            this.mount = mount;
        }
        
        public String getName()
        {
            return name;
        }
        
        public void setName(String name)
        {
            this.name = Tools.parseColors(name, false);
        }
        
        public boolean isNoHideName()
        {
            return noHideName;
        }
        
        public void setNoHideName(boolean noHideName)
        {
            this.noHideName = noHideName;
        }
        
        public int getHp()
        {
            return hp;
        }
        
        public void setHp(int hp)
        {
            this.hp = hp;
        }
        
        public int getMaxHp()
        {
            return maxHp;
        }
        
        public void setMaxHp(int maxHp)
        {
            this.maxHp = maxHp;
        }
        
        public boolean isBaby()
        {
            return baby;
        }
        
        public void setBaby(boolean baby)
        {
            this.baby = baby;
        }
        
        public boolean isAgeLock()
        {
            return ageLock;
        }
        
        public void setAgeLock(boolean lock)
        {
            this.ageLock = lock;
        }
        
        public ItemStack[] getEquip()
        {
            return equip;
        }
        
        public void setEquip(ItemStack[] equip)
        {
            this.equip = equip;
        }
        
        public float[] getDrop()
        {
            return drop;
        }
        
        public void setDrop(float[] drop)
        {
            this.drop = drop;
        }
        
        public List<PotionEffect> getPotionEffects()
        {
            return potions;
        }
        
        public void setPotionEFfects(List<PotionEffect> effects)
        {
            if(effects == null)
            {
                potions.clear();
            }
            else
            {
                potions = effects;
            }
        }
        
        public void addPotionEffect(PotionEffectType type, float duration, int amplifier)
        {
            potions.add(new PotionEffect(type, (int)Math.ceil((duration * 20) / type.getDurationModifier()), amplifier));
        }
        
        public float getOnFire()
        {
            return onFire;
        }
        
        public void setOnFire(float onFire)
        {
            this.onFire = onFire;
        }
        
        public Boolean getPickup()
        {
            return pickup;
        }
        
        public void setPickup(Boolean pickup)
        {
            this.pickup = pickup;
        }
        
        public boolean isNoSit()
        {
            return noSit;
        }
        
        public void setNoSit(boolean noSit)
        {
            this.noSit = noSit;
        }
        
        public boolean isAngry()
        {
            return angry;
        }
        
        public void setAngry(boolean angry)
        {
            this.angry = angry;
        }
        
        public Ocelot.Type getCat()
        {
            return cat;
        }
        
        public void setCat(Ocelot.Type cat)
        {
            this.cat = cat;
        }
        
        public DyeColor getColor()
        {
            return color;
        }
        
        public void setColor(DyeColor color)
        {
            this.color = color;
        }
        
        public boolean isShearedSheep()
        {
            return shearedSheep;
        }
        
        public void setShearedSheep(boolean shearedSheep)
        {
            this.shearedSheep = shearedSheep;
        }
        
        public SkeletonType getSkeleton()
        {
            return skeleton;
        }
        
        public void setSkeleton(SkeletonType skeleton)
        {
            this.skeleton = skeleton;
        }
        
        public boolean isZombieVillager()
        {
            return zombieVillager;
        }
        
        public void setZombieVillager(boolean zombieVillager)
        {
            this.zombieVillager = zombieVillager;
        }
        
        public Villager.Profession getVillager()
        {
            return villager;
        }
        
        public void setVillager(Villager.Profession villager)
        {
            this.villager = villager;
        }
        
        public boolean isPoweredCreeper()
        {
            return poweredCreeper;
        }
        
        public void setPoweredCreeper(boolean poweredCreeper)
        {
            this.poweredCreeper = poweredCreeper;
        }
        
        public boolean isPlayerIronGolem()
        {
            return playerIronGolem;
        }
        
        public void setPlayerIronGolem(boolean playerIronGolem)
        {
            this.playerIronGolem = playerIronGolem;
        }
        
        public int getPigAnger()
        {
            return pigAnger;
        }
        
        public void setPigAnger(int anger)
        {
            this.pigAnger = anger;
        }
        
        public boolean isMountNext()
        {
            return mountNext;
        }
        
        public void setMountNext(boolean mountNext)
        {
            this.mountNext = mountNext;
        }
        
        public boolean isHit()
        {
            return hit;
        }
        
        public void setHit(boolean hit)
        {
            this.hit = hit;
        }
        
        public boolean isNoBreed()
        {
            return noBreed;
        }
        
        public void setNoBreed(boolean noBreed)
        {
            this.noBreed = noBreed;
        }
    }
    
    private List<Customization> spawn = new ArrayList<Customization>();
    
    public FlagSummon()
    {
    }
    
    public FlagSummon(FlagSummon flag)
    {
        for(Customization c : flag.spawn)
        {
            spawn.add(c.clone());
        }
    }
    
    @Override
    public FlagSummon clone()
    {
        return new FlagSummon(this);
    }
    
    @Override
    public FlagType getType()
    {
        return TYPE;
    }
    
    public List<Customization> getSpawnList()
    {
        return spawn;
    }
    
    public void setSpawnList(List<Customization> list)
    {
        if(list == null)
        {
            this.remove();
        }
        else
        {
            this.spawn = list;
        }
    }
    
    public void addSpawn(Customization spawn)
    {
        Validate.notNull(spawn, "'spawn' can not be null!");
        
        this.spawn.add(spawn);
    }
    
    @Override
    protected boolean onParse(String value)
    {
        String[] split = value.split("\\|");
        
        value = split[0].trim();
        EntityType type = Tools.parseEnum(value, EntityType.values());
        
        if(type == null || !type.isAlive())
        {
            ErrorReporter.error("The " + getType() + " flag has invalid creature: " + value, "Look in '" + Files.FILE_INFO_NAMES + "' at 'ENTITY TYPES' section for ALIVE entities.");
            return false;
        }
        
        Customization c = new Customization(type);
        
        if(split.length > 1)
        {
            for(int n = 1; n < split.length; n++)
            {
                String original = split[n].trim();
                value = original.toLowerCase();
                
                if(value.equals("noremove"))
                {
                    c.setNoRemove(true);
                }
                else if(value.equals("noeffect"))
                {
                    c.setNoEffect(true);
                }
                else if(value.equals("target"))
                {
                    c.setTarget(true);
                }
                else if(value.equals("nohidename"))
                {
                    c.setNoHideName(true);
                }
                else if(value.equals("mountnext"))
                {
                    c.setMountNext(true);
                }
                else if(value.equals("angry"))
                {
                    switch(type)
                    {
                        case WOLF:
                        case PIG_ZOMBIE:
                            break;
                        
                        default:
                            ErrorReporter.warning("Flag " + getType() + " has 'angry' on unsupported creature!");
                            continue;
                    }
                    
                    c.setAngry(true);
                }
                else if(value.equals("shearedsheep"))
                {
                    if(type != EntityType.SHEEP)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'shearedsheep' on non-sheep creature!");
                        continue;
                    }
                    
                    c.setShearedSheep(true);
                }
                else if(value.equals("zombievillager"))
                {
                    if(type != EntityType.ZOMBIE)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'zombievillager' on non-zombie creature!");
                        continue;
                    }
                    
                    c.setZombieVillager(true);
                }
                else if(value.equals("poweredcreeper"))
                {
                    if(type != EntityType.CREEPER)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'poweredcreeper' on non-creeper creature!");
                        continue;
                    }
                    
                    c.setPoweredCreeper(true);
                }
                else if(value.equals("playerirongolem"))
                {
                    if(type != EntityType.IRON_GOLEM)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'playerirongolem' on non-irongolem creature!");
                        continue;
                    }
                    
                    c.setPlayerIronGolem(true);
                }
                else if(value.equals("hit"))
                {
                    c.setHit(true);
                }
                else if(value.equals("baby"))
                {
                    switch(type)
                    {
                        case CHICKEN:
                        case COW:
                        case MUSHROOM_COW:
                        case OCELOT:
                        case PIG:
                        case SHEEP:
                        case VILLAGER:
                        case WOLF:
                        case ZOMBIE: // has set/getBaby() but does not implement Ageable
                            break;
                        
                        default:
                            ErrorReporter.warning("Flag " + getType() + " has 'baby' set on unsupported creature!");
                            continue;
                    }
                    
                    c.setBaby(true);
                }
                else if(value.equals("agelock"))
                {
                    switch(type)
                    {
                        case CHICKEN:
                        case COW:
                        case MUSHROOM_COW:
                        case OCELOT:
                        case PIG:
                        case SHEEP:
                        case VILLAGER:
                        case WOLF:
                            break;
                        
                        default:
                            ErrorReporter.warning("Flag " + getType() + " has 'agelock' set on unsupported creature!");
                            continue;
                    }
                    
                    c.setAgeLock(true);
                }
                else if(value.equals("nobreed"))
                {
                    switch(type)
                    {
                        case CHICKEN:
                        case COW:
                        case MUSHROOM_COW:
                        case OCELOT:
                        case PIG:
                        case SHEEP:
                        case VILLAGER:
                        case WOLF:
                            break;
                        
                        default:
                            ErrorReporter.warning("Flag " + getType() + " has 'nobreed' set on unsupported creature!");
                            continue;
                    }
                    
                    c.setNoBreed(true);
                }
                else if(value.startsWith("pickup"))
                {
                    value = value.substring("pickup".length()).trim();
                    
                    if(value.isEmpty())
                    {
                        c.setPickup(true);
                    }
                    else
                    {
                        c.setPickup(value.equals("true"));
                    }
                }
                else if(value.startsWith("pet"))
                {
                    switch(type)
                    {
                        case WOLF:
                        case OCELOT:
                            break;
                        
                        default:
                            ErrorReporter.warning("Flag " + getType() + " has 'pet' on untameable creature!");
                            continue;
                    }
                    
                    c.setPet(true);
                    
                    if(value.length() > "pet".length())
                    {
                        value = value.substring("pet".length()).trim();
                        
                        if(value.equals("nosit"))
                        {
                            c.setNoSit(true);
                        }
                        else
                        {
                            ErrorReporter.warning("Flag " + getType() + " has 'pet' argument with unknown value: " + value);
                        }
                    }
                }
                else if(value.startsWith("saddle"))
                {
                    if(type != EntityType.PIG)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'saddle' on non-pig creature!");
                        continue;
                    }
                    
                    c.setSaddle(true);
                    
                    if(value.length() > "saddle".length())
                    {
                        value = value.substring("saddle".length()).trim();
                        
                        if(value.equals("mount"))
                        {
                            c.setMount(true);
                        }
                        else
                        {
                            ErrorReporter.warning("Flag " + getType() + " has 'saddle' argument with unknown value: " + value);
                        }
                    }
                }
                else if(value.startsWith("chance"))
                {
                    value = value.substring("chance".length()).trim();
                    
                    if(value.charAt(value.length() - 1) == '%')
                    {
                        value = value.substring(0, value.length() - 1);
                    }
                    
                    try
                    {
                        c.setChance(Float.valueOf(value));
                    }
                    catch(NumberFormatException e)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'chance' argument with invalid number: " + value);
                        continue;
                    }
                }
                else if(value.startsWith("num"))
                {
                    value = value.substring("num".length()).trim();
                    
                    try
                    {
                        c.setNum(Integer.valueOf(value));
                    }
                    catch(NumberFormatException e)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'num' argument with invalid value number: " + value);
                    }
                }
                else if(value.startsWith("spread"))
                {
                    value = value.substring("spread".length()).trim();
                    
                    try
                    {
                        c.setSpread(Integer.valueOf(value));
                    }
                    catch(NumberFormatException e)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'spread' argument with invalid value number: " + value);
                    }
                }
                else if(value.startsWith("onfire"))
                {
                    value = value.substring("onfire".length()).trim();
                    
                    try
                    {
                        c.setOnFire(Float.valueOf(value) * 20.0f);
                    }
                    catch(NumberFormatException e)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'onfire' argument with invalid value number: " + value);
                    }
                }
                else if(value.startsWith("color"))
                {
                    switch(type)
                    {
                        case SHEEP:
                        case WOLF:
                            break;
                        
                        default:
                            ErrorReporter.warning("Flag " + getType() + " has 'color' on unsupported creature!");
                            continue;
                    }
                    
                    value = value.substring("color".length()).trim();
                    
                    c.setColor(Tools.parseEnum(value, DyeColor.values()));
                    
                    if(c.getColor() == null)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'color' argument with invalid dye color: " + value);
                    }
                }
                else if(value.startsWith("villager"))
                {
                    if(type != EntityType.VILLAGER)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'villager' argument on non-villager creature!");
                        continue;
                    }
                    
                    value = value.substring("villager".length()).trim();
                    
                    c.setVillager(Tools.parseEnum(value, Villager.Profession.values()));
                    
                    if(c.getVillager() == null)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'villager' argument with invalid type: " + value);
                    }
                }
                else if(value.startsWith("skeleton"))
                {
                    if(type != EntityType.SKELETON)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'skeleton' argument on non-skeleton creature!");
                        continue;
                    }
                    
                    value = value.substring("skeleton".length()).trim();
                    
                    c.setSkeleton(Tools.parseEnum(value, SkeletonType.values()));
                    
                    if(c.getSkeleton() == null)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'skeleton' argument with invalid type: " + value);
                    }
                }
                else if(value.startsWith("cat"))
                {
                    if(type != EntityType.OCELOT)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'cat' argument on non-ocelot creature!");
                        continue;
                    }
                    
                    value = value.substring("cat".length()).trim();
                    
                    c.setCat(Tools.parseEnum(value, Ocelot.Type.values()));
                    
                    if(c.getCat() == null)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'cat' argument with invalid type: " + value);
                    }
                }
                else if(value.startsWith("name"))
                {
                    value = original.substring("name".length()).trim();
                    
                    c.setName(value);
                }
                else if(value.startsWith("hp"))
                {
                    value = value.substring("hp".length()).trim();
                    
                    String[] args = value.split(" ");
                    
                    value = args[0].trim();
                    
                    try
                    {
                        c.setHp(Integer.valueOf(value));
                    }
                    catch(NumberFormatException e)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'hp' argument with invalid number: " + value);
                        continue;
                    }
                    
                    if(args.length > 1)
                    {
                        value = args[1].trim();
                        
                        try
                        {
                            c.setMaxHp(Integer.valueOf(value));
                        }
                        catch(NumberFormatException e)
                        {
                            ErrorReporter.warning("Flag " + getType() + " has 'hp' argument with invalid number for maxhp: " + value);
                            continue;
                        }
                    }
                }
                else if(value.startsWith("potion"))
                {
                    value = value.substring("potion".length()).trim();
                    String[] args = value.split(" ");
                    value = args[0].trim();
                    
                    PotionEffectType effect = PotionEffectType.getByName(value); // Tools.parseEnum(value, PotionEffectType.values());
                    
                    if(effect == null)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has 'potion' argument with invalid type: " + value);
                        continue;
                    }
                    
                    float duration = 1;
                    int amplifier = 0;
                    
                    if(args.length > 1)
                    {
                        value = args[1].trim();
                        
                        try
                        {
                            duration = Float.valueOf(value);
                        }
                        catch(NumberFormatException e)
                        {
                            ErrorReporter.warning("Flag " + getType() + " has 'potion' argument with invalid number for duration: " + value);
                            continue;
                        }
                    }
                    
                    if(args.length > 2)
                    {
                        value = args[2].trim();
                        
                        try
                        {
                            amplifier = Integer.valueOf(value);
                        }
                        catch(NumberFormatException e)
                        {
                            ErrorReporter.warning("Flag " + getType() + " has 'potion' argument with invalid number for amplifier: " + value);
                            continue;
                        }
                    }
                    
                    c.addPotionEffect(effect, duration, amplifier);
                }
                else if(value.startsWith("hand") || value.startsWith("hold") || value.startsWith("head") || value.startsWith("helmet") || value.startsWith("chest") || value.startsWith("leg") || value.startsWith("feet") || value.startsWith("boot"))
                {
                    int index = -1;
                    
                    switch(value.charAt(0))
                    {
                        case 'h':
                            switch(value.charAt(1))
                            {
                                case 'e':
                                    index = 0;
                                    break;
                                
                                case 'o':
                                case 'a':
                                    index = 4;
                                    break;
                            }
                            break;
                        
                        case 'c':
                            index = 1;
                            break;
                        
                        case 'l':
                            index = 2;
                            break;
                        
                        case 'b':
                        case 'f':
                            index = 3;
                            break;
                    }
                    
                    if(index < 0)
                    {
                        ErrorReporter.warning("Flag " + getType() + " has unknown argument: " + value);
                        continue;
                    }
                    
                    int i = value.indexOf(' ');
                    String[] args = value.substring(i + 1).trim().split(" ");
                    value = args[0].trim();
                    
                    ItemStack item = Tools.parseItem(value, 0);
                    
                    if(item == null)
                    {
                        continue;
                    }
                    
                    c.getEquip()[index] = item;
                    
                    if(args.length > 1)
                    {
                        value = args[1].trim();
                        
                        if(value.charAt(value.length() - 1) == '%')
                        {
                            value = value.substring(0, value.length() - 1);
                        }
                        
                        try
                        {
                            c.getDrop()[index] = Math.min(Math.max(Float.valueOf(value), 0), 100);
                        }
                        catch(NumberFormatException e)
                        {
                            ErrorReporter.warning("Flag " + getType() + " has 'chance' argument with invalid number: " + value);
                            continue;
                        }
                    }
                }
                else
                {
                    ErrorReporter.warning("Flag " + getType() + " has unknown argument: " + value);
                }
            }
        }
        
        if(type == EntityType.WOLF)
        {
            if(c.isPet())
            {
                if(c.isAngry())
                {
                    c.setAngry(false);
                    ErrorReporter.warning("Flag " + getType() + " has 'angry' with 'pet' on wolf! Argument 'angry' ignored.");
                }
            }
            else
            {
                if(c.getColor() != null)
                {
                    c.setColor(null);
                    ErrorReporter.warning("Flag " + getType() + " has 'color' argument without wolf being a pet, ignored.");
                }
            }
        }
        
        addSpawn(c);
        
        return true;
    }
    
    @Override
    protected void onCrafted(Args a)
    {
        if(!a.hasLocation())
        {
            a.addCustomReason("Needs location!");
            return;
        }
        
        for(Customization c : spawn)
        {
            if(c.pet || c.target || (c.saddle && c.mount))
            {
                if(!a.hasPlayer())
                {
                    a.addCustomReason("Needs player!");
                    return;
                }
                
                break;
            }
        }
        
        Location l = a.location();
        
        if(l.getX() == l.getBlockX())
        {
            l.add(0.5, 1.5, 0.5);
        }
        
        List<LivingEntity> toMount = null;
        
        for(Customization c : spawn)
        {
            if(c.chance < 100.0f && c.chance < (RecipeManager.random.nextFloat() * 100))
            {
                continue;
            }
            
            List<LivingEntity> spawned = c.spawn(l, a.player());
            
            if(toMount != null)
            {
                for(int i = 0; i < Math.min(spawned.size(), toMount.size()); i++)
                {
                    spawned.get(i).setPassenger(toMount.get(i));
                }
            }
            
            toMount = c.mountNext ? spawned : null;
        }
    }
}
