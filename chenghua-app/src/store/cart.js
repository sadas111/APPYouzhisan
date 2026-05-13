import { reactive, computed } from 'vue'

// Singleton State
const state = reactive({
  items: []
})

export const useCartStore = () => {
  const addToCart = (product) => {
    const existingItem = state.items.find(item => item.id === product.id)
    if (existingItem) {
      existingItem.quantity++
    } else {
      state.items.push({
        ...product,
        quantity: 1
      })
    }
  }

  const removeFromCart = (productId) => {
    const index = state.items.findIndex(item => item.id === productId)
    if (index > -1) {
      state.items.splice(index, 1)
    }
  }

  const increaseQuantity = (productId) => {
    const item = state.items.find(item => item.id === productId)
    if (item) {
      item.quantity++
    }
  }

  const decreaseQuantity = (productId) => {
    const item = state.items.find(item => item.id === productId)
    if (item) {
      if (item.quantity > 1) {
        item.quantity--
      } else {
        removeFromCart(productId)
      }
    }
  }

  const clearCart = () => {
    state.items = []
  }

  const totalCount = computed(() => {
    return state.items.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return state.items.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
  })

  return {
    state,
    addToCart,
    removeFromCart,
    increaseQuantity,
    decreaseQuantity,
    clearCart,
    totalCount,
    totalPrice
  }
}
