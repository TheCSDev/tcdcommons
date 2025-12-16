package com.thecsdev.commonmc.api.client.gui.misc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thecsdev.common.properties.BooleanProperty;
import com.thecsdev.common.properties.DoubleProperty;
import com.thecsdev.common.properties.NotNullProperty;
import com.thecsdev.common.util.annotations.Virtual;
import com.thecsdev.commonmc.TCDCommons;
import com.thecsdev.commonmc.TCDCommonsConfig;
import com.thecsdev.commonmc.api.client.gui.TElement;
import com.thecsdev.commonmc.api.client.gui.render.TGuiGraphics;
import com.thecsdev.commonmc.world.sandbox.SandboxLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * {@link TElement} that renders an entity on the screen.
 */
public @Virtual class TEntityElement extends TElement
{
	// ================================================== ==================================================
	//                                     TEntityElement IMPLEMENTATION
	// ================================================== ==================================================
	private final NotNullProperty<EntityType<?>> entityType    = new NotNullProperty<>(EntityType.MARKER);
	private final BooleanProperty                followsCursor = new BooleanProperty(true);
	private final DoubleProperty                 entityScale   = new DoubleProperty(1d);
	// --------------------------------------------------
	private @Nullable Entity displayEntity; //used for rendering. 'null' = 'something went wrong'.
	// ==================================================
	public TEntityElement(@NotNull EntityType<?> entityType) {
		this();
		this.entityType.set(entityType, TEntityElement.class);
	}
	public TEntityElement() {
		//this element should not be focusable or hoverable
		focusableProperty().set(false, TEntityElement.class);
		hoverableProperty().set(false, TEntityElement.class);
		//automatic display data refreshing when relevant properties update
		boundsProperty().addChangeListener((p, o, n) -> { if(!o.hasSameSize(n)) refresh(); });
		this.entityType.addChangeListener((p, o, n) -> refresh());
		//the initial display data refresh
		refresh();
	}
	// --------------------------------------------------
	/**
	 * Refereshes the {@link #displayEntity} value. Called automatically
	 * whenever {@link #entityTypeProperty()} value changes.
	 */
	private final @ApiStatus.Internal void refresh() {
		try {
			this.displayEntity = EntityProvider.getOrCreate(this.entityType.get());
		} catch(Exception e) {
			if(TCDCommonsConfig.FLAG_DEV_ENV)
				TCDCommons.LOGGER.error("Failed to create GUI entity instance for {}", this.entityType.get(), e);
			this.displayEntity = null;
		}
	}
	// ==================================================
	/**
	 * Returns the {@link NotNullProperty} holding the {@link EntityType}
	 * that is to be rendered by this {@link TEntityElement}.
	 */
	public final NotNullProperty<EntityType<?>> entityTypeProperty() { return this.entityType; }

	/**
	 * Returns the {@link BooleanProperty} that determines whether
	 * this the rendered {@link Entity}'s on-screen rotation follows
	 * the cursor location.
	 */
	public final BooleanProperty followsCursorProperty() { return this.followsCursor; }

	/**
	 * Returns the {@link DoubleProperty} that determines the scale
	 * at which the rendered {@link Entity} is drawn.
	 */
	public final DoubleProperty entityScaleProperty() { return this.entityScale; }
	// ==================================================
	public @Virtual @Override void renderCallback(@NotNull TGuiGraphics pencil) {
		final var bb = this.getBounds();
		if(this.displayEntity != null) {
			pencil.pushScissors(bb.x, bb.y, bb.width, bb.height);
			try {
				//attempt to render the entity (some modded ones can and do throw)
				pencil.renderEntity(
						this.displayEntity, bb.x, bb.y, bb.width, bb.height,
						this.entityScale.getD(), this.followsCursor.getZ());
			} catch(Exception e) {
				//kill the rendering if something goes wrong, as some modded entities
				//do not support being drawn on-screen. that is unfortunate
				if(TCDCommonsConfig.FLAG_DEV_ENV)
					TCDCommons.LOGGER.error("Failed to render GUI Entity {}", this.displayEntity, e);
				this.displayEntity = null;
			}
			pencil.popScissors();
		} else {
			pencil.drawMissingNo(bb.x, bb.y, bb.width, bb.height, -1);
		}
	}
	// ==================================================
	/**
	 * Cached {@link Entity} instance used by this {@link TEntityElement} for
	 * rendering said {@link Entity} on the GUI screen.
	 * <p>
	 * This value is assigned automatically. You should avoid trying to set
	 * this value, instead only read it in case you are overriding
	 * {@link #renderCallback(TGuiGraphics)}.
	 * <p>
	 * In addition, this value is {@code null} whenever something goes wrong
	 * when attempting to create and/or render the {@link Entity} instance.
	 * This is usually due to {@link Exception}s being raised during attemts
	 * to render the display {@link Entity}.
	 *
	 * @see #renderCallback(TGuiGraphics)
	 * @see EntityProvider
	 */
	public final @Nullable Entity getDisplayEntity() { return this.displayEntity; }
	// ================================================== ==================================================
	//                                     EntityProvider IMPLEMENTATION
	// ================================================== ==================================================
	/**
	 * Utility class for providing {@link Entity} instances for rendering in GUIs.
	 * These entities do not exist in a "real" {@link Level}. Instead, this uses a
	 * sandbox {@link Level} to create entities in.
	 */
	public static final @ApiStatus.Internal class EntityProvider
	{
		// ==================================================
		private static final Cache<EntityType<?>, Entity> CACHE = CacheBuilder.newBuilder().weakValues().build();
		// ==================================================
		private EntityProvider() {}
		// ==================================================
		/**
		 * Returns an instance of the given {@link EntityType}.
		 * @param entityType The {@link EntityType} to create an instance of.
		 * @param <E> The type of the {@link Entity}.
		 * @return An instance of the given {@link EntityType}.
		 * @throws NullPointerException If the argument is {@code null} or if the entity could not be created.
		 * @throws RuntimeException If an error occurs during {@link Entity} instance creation.
		 */
		@SuppressWarnings("unchecked")
		public static final @NotNull <E extends Entity> E getOrCreate(@NotNull EntityType<E> entityType)
				throws NullPointerException, RuntimeException {
			//not null enforcement
			Objects.requireNonNull(entityType);
			//create and return the entity
			try {
				//handle player entity type
				if(entityType == EntityType.PLAYER)
					return (E) Objects.requireNonNull(
							Minecraft.getInstance().player,
							"Local player instance is not present");
				//handle all other types
				return (E) CACHE.get(entityType, () -> Objects.requireNonNull(
					entityType.create(SandboxLevel.INSTANCE, EntitySpawnReason.MOB_SUMMONED),
					"Entity creator factory returned 'null'."));
			} catch(Exception e) {
				throw new RuntimeException("Failed to create entity of type " + entityType, e);
			}
		}
		// ==================================================
	}
	// ================================================== ==================================================
}
