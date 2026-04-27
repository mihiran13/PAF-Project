/**
 * Smart Resource Recommendation Engine
 * Provides intelligent resource suggestions based on availability, 
 * user preferences, and historical booking patterns
 */

export const recommendedResourcesService = {
  /**
   * Calculate resource score based on multiple factors
   * @param {Object} resource - Resource object
   * @param {Array} bookings - User's booking history
   * @param {Object} preferences - User preferences
   * @param {Date} requestedDate - Date for booking
   * @returns {number} Score from 0-100
   */
  calculateResourceScore(resource, bookings = [], preferences = {}, requestedDate = new Date()) {
    let score = 50 // Base score

    // Factor 1: Availability (30 points)
    if (resource.status === 'ACTIVE') score += 20
    if (resource.capacity > 0) score += 10

    // Factor 2: User preference match (25 points)
    if (preferences.preferredType && preferences.preferredType === resource.type) score += 15
    if (preferences.preferredLocation && resource.location?.includes(preferences.preferredLocation)) score += 10

    // Factor 3: Historical performance (25 points)
    const userBookingsForType = bookings.filter(b => b.resourceType === resource.type)
    const successRate = userBookingsForType.length > 0 
      ? (userBookingsForType.filter(b => b.status === 'COMPLETED').length / userBookingsForType.length)
      : 0.5
    score += Math.round(successRate * 20)

    // Factor 4: Popularity & demand (10 points)
    const demandLevel = resource.recentBookingCount || 0
    if (demandLevel < 2) score += 8
    else if (demandLevel < 5) score += 5
    else score += 2 // High demand = slightly lower score for availability

    // Factor 5: Booking patterns (10 points)
    const dayOfWeek = requestedDate.getDay()
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 6
    const isPreferredTime = new Date(requestedDate).getHours() >= 14 && new Date(requestedDate).getHours() <= 16

    if (isWeekend && resource.type === 'MEETING_ROOM') score += 5
    if (!isWeekend && resource.type === 'LECTURE_HALL') score += 5
    if (isPreferredTime) score += 3

    // Normalize to 0-100
    return Math.min(100, Math.max(0, score))
  },

  /**
   * Get top recommended resources
   * @param {Array} resources - Available resources
   * @param {Object} params - Filter parameters
   * @returns {Array} Sorted resources with recommendation scores
   */
  getRecommendedResources(resources = [], params = {}) {
    const {
      bookings = [],
      preferences = {},
      requestedDate = new Date(),
      capacity = 0,
      type = null,
      limit = 5
    } = params

    // Filter resources first
    let filtered = resources.filter(r => {
      if (type && r.type !== type) return false
      if (capacity && r.capacity < capacity) return false
      if (r.status !== 'ACTIVE') return false
      return true
    })

    // Calculate scores and sort
    const scored = filtered
      .map(resource => ({
        ...resource,
        recommendationScore: this.calculateResourceScore(resource, bookings, preferences, requestedDate),
        reason: this.getRecommendationReason(resource, preferences, type)
      }))
      .sort((a, b) => b.recommendationScore - a.recommendationScore)

    return scored.slice(0, limit)
  },

  /**
   * Get human-readable reason for recommendation
   * @param {Object} resource - Resource object
   * @param {Object} preferences - User preferences
   * @param {string} requestedType - Requested resource type
   * @returns {string} Reason text
   */
  getRecommendationReason(resource, preferences = {}, requestedType = null) {
    if (preferences.preferredType === resource.type) {
      return `Your preferred resource type: ${resource.type}`
    }

    if (preferences.preferredLocation && resource.location?.includes(preferences.preferredLocation)) {
      return `Near your preferred location: ${preferences.preferredLocation}`
    }

    if (resource.capacity > 30) {
      return 'High capacity available'
    }

    return 'Available and suitable'
  },

  /**
   * Get alternative resources if primary choice is not available
   * @param {Object} resource - Primary resource
   * @param {Array} allResources - All available resources
   * @param {number} limit - Number of alternatives to return
   * @returns {Array} Alternative resources
   */
  getAlternatives(resource, allResources = [], limit = 3) {
    return allResources
      .filter(r => 
        r.id !== resource.id && 
        r.type === resource.type && 
        r.status === 'ACTIVE' &&
        r.capacity >= resource.capacity
      )
      .slice(0, limit)
  },

  /**
   * Check if a resource is trending (frequently booked)
   * @param {Object} resource - Resource object
   * @returns {boolean} True if resource is trending
   */
  isTrending(resource) {
    const recentBookingThreshold = 5
    return (resource.recentBookingCount || 0) >= recentBookingThreshold
  },

  /**
   * Get predicted availability for a resource
   * @param {Object} resource - Resource object
   * @param {Date} date - Date to check
   * @returns {Object} Availability info with confidence score
   */
  getPredictedAvailability(resource, date = new Date()) {
    const dayOfWeek = date.getDay()
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 6
    const hour = date.getHours()

    // Simple heuristic-based prediction
    let availabilityScore = 75 // Base availability

    // Factor in day of week
    if (!isWeekend && hour >= 9 && hour <= 17) {
      availabilityScore -= 30 // Peak hours on weekdays
    }

    // Factor in resource popularity
    if (this.isTrending(resource)) {
      availabilityScore -= 20
    }

    // Factor in resource type
    const typeBookingRates = {
      'LECTURE_HALL': 0.8,
      'LAB': 0.7,
      'MEETING_ROOM': 0.6,
      'EQUIPMENT': 0.5
    }

    const rate = typeBookingRates[resource.type] || 0.6
    availabilityScore = Math.round(availabilityScore * (1 - rate))

    return {
      availabilityScore: Math.max(0, Math.min(100, availabilityScore)),
      isPeakHour: !isWeekend && hour >= 9 && hour <= 17,
      isWeekend,
      confidence: availabilityScore > 70 ? 'high' : availabilityScore > 40 ? 'medium' : 'low',
      suggestion: availabilityScore > 70 
        ? 'Great chances of booking this resource'
        : availabilityScore > 40
        ? 'May need to check availability or try alternative times'
        : 'Consider booking alternative resource or different time'
    }
  },

  /**
   * Get optimal booking windows for a resource
   * @param {Object} resource - Resource object
   * @param {Date} startDate - Start date to analyze
   * @returns {Array} Array of optimal booking windows
   */
  getOptimalBookingWindows(resource, startDate = new Date()) {
    const windows = []

    // Analyze next 7 days
    for (let i = 0; i < 7; i++) {
      const date = new Date(startDate)
      date.setDate(date.getDate() + i)
      date.setHours(0, 0, 0, 0)

      const dayOfWeek = date.getDay()
      const isWeekend = dayOfWeek === 0 || dayOfWeek === 6

      // Define optimal times based on resource type
      let optimalTimes = []

      if (resource.type === 'LECTURE_HALL') {
        optimalTimes = isWeekend ? [[9, 18]] : [[8, 10], [14, 16]]
      } else if (resource.type === 'LAB') {
        optimalTimes = [[9, 12], [13, 16]]
      } else if (resource.type === 'MEETING_ROOM') {
        optimalTimes = [[10, 12], [14, 17]]
      } else {
        optimalTimes = [[10, 16]]
      }

      optimalTimes.forEach(([start, end]) => {
        windows.push({
          date: date.toISOString().split('T')[0],
          startTime: `${start}:00`,
          endTime: `${end}:00`,
          availabilityScore: 85 + Math.random() * 15, // Simulated score
          label: `${isWeekend ? 'Weekend' : 'Weekday'} ${start}:00-${end}:00`
        })
      })
    }

    return windows
  },

  /**
   * Generate smart booking suggestion
   * @param {Object} params - Parameters for suggestion
   * @returns {Object} Booking suggestion with explanation
   */
  getSmartBookingSuggestion(params = {}) {
    const {
      resources = [],
      bookings = [],
      preferences = {},
      requiredCapacity = 20,
      requestedDate = new Date(),
      resourceType = null
    } = params

    // Get top recommendation
    const topRecommendation = this.getRecommendedResources(resources, {
      bookings,
      preferences,
      requestedDate,
      capacity: requiredCapacity,
      type: resourceType,
      limit: 1
    })[0]

    if (!topRecommendation) {
      return {
        suggested: null,
        message: 'No suitable resources available at this time',
        alternatives: []
      }
    }

    // Get optimal windows
    const optimalWindows = this.getOptimalBookingWindows(topRecommendation, requestedDate)
    const bestWindow = optimalWindows[0]

    // Get alternatives
    const alternatives = this.getAlternatives(topRecommendation, resources, 2)

    return {
      suggested: topRecommendation,
      recommendationScore: topRecommendation.recommendationScore,
      reason: topRecommendation.reason,
      optimalBookingTime: bestWindow,
      alternatives,
      predictedAvailability: this.getPredictedAvailability(topRecommendation, requestedDate),
      message: `We recommend ${topRecommendation.name} at ${bestWindow.startTime} - ${bestWindow.endTime}. ${topRecommendation.reason}.`
    }
  }
}

export default recommendedResourcesService
