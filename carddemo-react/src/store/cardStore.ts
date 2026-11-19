import { create } from 'zustand'
import { Card, CardStatus } from '@/types'
import { cardService } from '@/services/cardService'
import { ApiError } from '@/types'

interface CardFilter {
  status?: CardStatus
  accountId?: string
}

interface CardState {
  selectedCard: Card | null
  cards: Card[]
  filter: CardFilter
  loading: boolean
  error: string | null

  // Actions
  setSelectedCard: (card: Card | null) => void
  setCards: (cards: Card[]) => void
  setFilter: (filter: CardFilter) => void
  setLoading: (loading: boolean) => void
  setError: (error: string | null) => void
  clearError: () => void

  // Async loaders
  loadCard: (number: string) => Promise<void>
  loadCardsByAccount: (accountId: string) => Promise<void>
  issueCard: (data: Omit<Card, 'id' | 'status'> & { status?: CardStatus }) => Promise<void>
}

export const useCardStore = create<CardState>((set) => ({
  selectedCard: null,
  cards: [],
  filter: {},
  loading: false,
  error: null,

  setSelectedCard: (card) => set({ selectedCard: card }),
  setCards: (cards) => set({ cards }),
  setFilter: (filter) => set({ filter }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  clearError: () => set({ error: null }),

  loadCard: async (number) => {
    set({ loading: true, error: null })
    try {
      const card = await cardService.getCard(number)
      set({ selectedCard: card, loading: false })
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to load card'
      set({ error: message, loading: false })
    }
  },

  loadCardsByAccount: async (accountId) => {
    set({ loading: true, error: null })
    try {
      const cards = await cardService.getCardsByAccount(accountId)
      set({ cards, loading: false, filter: { accountId } })
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to load cards'
      set({ error: message, loading: false })
    }
  },

  issueCard: async (data) => {
    set({ loading: true, error: null })
    try {
      const card = await cardService.issueCard(data as Parameters<typeof cardService.issueCard>[0])
      set((state) => ({
        cards: [...state.cards, card],
        selectedCard: card,
        loading: false,
      }))
    } catch (error) {
      const message = (error as ApiError)?.message || 'Failed to issue card'
      set({ error: message, loading: false })
    }
  },
}))
