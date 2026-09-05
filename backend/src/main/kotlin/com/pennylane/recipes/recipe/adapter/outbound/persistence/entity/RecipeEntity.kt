package com.pennylane.recipes.recipe.adapter.outbound.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.hibernate.annotations.BatchSize

@Entity
@Table(name = "recipe")
class RecipeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    var name: String = "",
    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "recipe_ingredient",
        joinColumns = [JoinColumn(name = "recipe_id")],
        inverseJoinColumns = [JoinColumn(name = "ingredient_id")],
    )
    @BatchSize(size = 50)
    @OrderBy("name")
    var ingredients: MutableSet<IngredientEntity> = mutableSetOf(),
)
