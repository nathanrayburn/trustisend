/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["../resources/templates/**/*.{html,js}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Source Sans Pro', 'sans-serif'],
      },
      animation: {
        fadeIn: "fadeIn 15s ease-in forwards",
        slideInLeft: "slideInLeft 3.8s ease-out forwards", // Ajouté slideInLeft
        slideInRight: "slideInRight 3.8s ease-out forwards", // Ajouté slideInRight
      },
      keyframes: {
        fadeIn: {
          "0%": { opacity: 0 },
          "100%": { opacity: 1 },
        },
        slideInLeft: {
          "0%": { opacity: 0, transform: "translateX(-100%)" },
          "100%": { opacity: 1, transform: "translateX(0)" },
        },
        slideInRight: {
          "0%": { opacity: 0, transform: "translateX(100%)" },
          "100%": { opacity: 1, transform: "translateX(0)" },
        },
      },
    },
  },
  plugins: [],
  variants: {
    animation: ["motion-safe"],
  },
};
